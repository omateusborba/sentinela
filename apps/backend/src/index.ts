import { Hono, type Context } from "hono";
import { cors } from "hono/cors";
import type { FireHotspot, FiresQueryParams } from "@sentinela/shared";
import { buildFirmsUrl, fetchFirmsCsv, normalizeHotspots } from "./firms";
import { cacheKey, getCachedFires, setCachedFires } from "./cache";
import { computeRegionRisk } from "./risk";
import {
  isValidationError,
  parseBbox,
  parseDays,
  parseFiresQuery,
} from "./validation";
import { serveStatic } from "./assets";
import { fetchOsmTile } from "./tiles";

export interface Env {
  FIRMS_MAP_KEY: string;
  FIRES_CACHE: KVNamespace;
  ASSETS: Fetcher;
}

const app = new Hono<{ Bindings: Env }>();

const CORS_ORIGINS = [
  "http://localhost:5173",
  "https://app.sentinela.pages.dev",
  "https://sentinela-7jx.pages.dev",
];

function isAllowedCorsOrigin(origin: string): boolean {
  if (CORS_ORIGINS.includes(origin)) return true;
  try {
    const { protocol, hostname } = new URL(origin);
    return protocol === "https:" && hostname.endsWith(".pages.dev");
  } catch {
    return false;
  }
}

app.use(
  "*",
  cors({
    origin: (origin) =>
      origin && isAllowedCorsOrigin(origin) ? origin : null,
    allowMethods: ["GET", "OPTIONS"],
    allowHeaders: ["Content-Type"],
  }),
);

app.get("/health", (c) => c.json({ status: "ok" }));

app.get("/mobile-map", async (c) => {
  const assetPath = "/mobile-map.html";
  const response = await serveStatic(c.env.ASSETS, assetPath, c.req.raw);
  return new Response(response.body, {
    status: response.status,
    headers: {
      "Content-Type": "text/html; charset=utf-8",
      "Cache-Control": "no-cache, no-store, must-revalidate",
    },
  });
});

app.get("/leaflet/*", async (c) => {
  const path = new URL(c.req.url).pathname;
  return serveStatic(c.env.ASSETS, path, c.req.raw);
});

app.get("/api/tiles/:z/:x/:y", async (c) => {
  const { z, x, y } = c.req.param();
  const zNum = Number(z);
  const xNum = Number(x);
  const yRaw = y.replace(/\.png$/i, "");
  const yNum = Number(yRaw);
  if (
    !Number.isInteger(zNum) ||
    !Number.isInteger(xNum) ||
    !Number.isInteger(yNum) ||
    zNum < 0 ||
    zNum > 19
  ) {
    return c.json({ error: "invalid tile coordinates" }, 400);
  }
  return fetchOsmTile(String(zNum), String(xNum), String(yNum));
});

async function loadFires(
  c: Context<{ Bindings: Env }>,
  query: FiresQueryParams,
): Promise<FireHotspot[]> {
  const key = cacheKey(query.bbox, query.days, query.source);
  const cached = await getCachedFires(c.env.FIRES_CACHE, key);
  if (cached) {
    c.header("X-Cache", "HIT");
    return cached.hotspots;
  }

  if (!c.env.FIRMS_MAP_KEY?.trim()) {
    throw new Error("FIRMS_MAP_KEY is not configured");
  }

  const url = buildFirmsUrl(
    c.env.FIRMS_MAP_KEY,
    query.source,
    query.bbox,
    query.days,
  );
  const csv = await fetchFirmsCsv(url);
  const hotspots = normalizeHotspots(csv);

  await setCachedFires(c.env.FIRES_CACHE, key, hotspots);
  c.header("X-Cache", "MISS");
  return hotspots;
}

app.get("/api/fires", async (c) => {
  const query = parseFiresQuery(c.req.query());
  if (isValidationError(query)) {
    return c.json({ error: query.error }, 400);
  }

  try {
    const hotspots = await loadFires(c, query);
    return c.json({
      count: hotspots.length,
      bbox: query.bbox,
      days: query.days,
      source: query.source,
      hotspots,
    });
  } catch (err) {
    const message = err instanceof Error ? err.message : "Unknown error";
    return c.json({ error: message }, 502);
  }
});

app.get("/api/risk", async (c) => {
  const bboxResult = parseBbox(c.req.query("bbox"));
  if (isValidationError(bboxResult)) {
    return c.json({ error: bboxResult.error }, 400);
  }

  const daysResult = parseDays(c.req.query("days"), 3);
  if (isValidationError(daysResult)) {
    return c.json({ error: daysResult.error }, 400);
  }

  const query = parseFiresQuery(
    new URLSearchParams({
      bbox: c.req.query("bbox") ?? "",
      days: String(daysResult),
      source: c.req.query("source") ?? "VIIRS_NOAA20_NRT",
    }),
  );
  if (isValidationError(query)) {
    return c.json({ error: query.error }, 400);
  }

  try {
    const hotspots = await loadFires(c, query);
    const risk = computeRegionRisk(hotspots);
    return c.json({
      bbox: query.bbox,
      days: query.days,
      risk,
    });
  } catch (err) {
    const message = err instanceof Error ? err.message : "Unknown error";
    return c.json({ error: message }, 502);
  }
});

export default app;
