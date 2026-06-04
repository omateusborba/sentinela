import type { FireHotspot, FirmsSource } from "@sentinela/shared";
import { parseCsv } from "./csv";

const FIRMS_BASE = "https://firms.modaps.eosdis.nasa.gov/api/area/csv";

export function buildFirmsUrl(
  mapKey: string,
  source: FirmsSource,
  bbox: string,
  days: number,
): string {
  return `${FIRMS_BASE}/${mapKey}/${source}/${bbox}/${days}`;
}

export async function fetchFirmsCsv(url: string): Promise<string> {
  const response = await fetch(url, {
    headers: { Accept: "text/csv" },
  });

  if (!response.ok) {
    const body = (await response.text()).trim();
    if (body.includes("Invalid MAP_KEY")) {
      throw new Error(
        "FIRMS_MAP_KEY inválida ou ausente no Worker. Configure o secret na Cloudflare (Settings → Variables).",
      );
    }
    throw new Error(
      body
        ? `FIRMS API error: ${response.status} — ${body.slice(0, 120)}`
        : `FIRMS API error: ${response.status} ${response.statusText}`,
    );
  }

  return response.text();
}

export function normalizeHotspots(csvText: string): FireHotspot[] {
  const rows = parseCsv(csvText);
  return rows
    .map((row) => rowToHotspot(row))
    .filter((h): h is FireHotspot => h !== null);
}

function rowToHotspot(row: Record<string, string>): FireHotspot | null {
  const lat = parseFloat(row.latitude);
  const lon = parseFloat(row.longitude);
  if (!Number.isFinite(lat) || !Number.isFinite(lon)) return null;

  const acqDate = row.acq_date?.trim();
  const acqTime = row.acq_time?.trim();
  if (!acqDate || !acqTime) return null;

  const acquiredAt = toIso8601(acqDate, acqTime);
  if (!acquiredAt) return null;

  const id = hotspotId(lat, lon, acqDate, acqTime);
  const frpRaw = row.frp?.trim();
  const frp = frpRaw ? parseFloat(frpRaw) : null;

  return {
    id,
    latitude: lat,
    longitude: lon,
    acquiredAt,
    satellite: row.satellite?.trim() ?? "",
    instrument: row.instrument?.trim() ?? "",
    confidence: normalizeConfidence(row.confidence ?? ""),
    frp: frp !== null && Number.isFinite(frp) ? frp : null,
    dayNight: normalizeDayNight(row.daynight ?? ""),
  };
}

function toIso8601(acqDate: string, acqTime: string): string | null {
  const padded = acqTime.padStart(4, "0");
  const hours = padded.slice(0, 2);
  const minutes = padded.slice(2, 4);
  const hh = Number(hours);
  const mm = Number(minutes);
  if (!Number.isFinite(hh) || !Number.isFinite(mm) || hh > 23 || mm > 59) {
    return null;
  }
  const iso = `${acqDate}T${hours}:${minutes}:00Z`;
  const parsed = Date.parse(iso);
  return Number.isFinite(parsed) ? new Date(parsed).toISOString() : null;
}

function normalizeConfidence(raw: string): FireHotspot["confidence"] {
  const v = raw.toLowerCase().trim();
  if (v === "low" || v === "l") return "low";
  if (v === "high" || v === "h") return "high";
  if (v === "nominal" || v === "n") return "nominal";

  const n = parseInt(v, 10);
  if (!Number.isNaN(n)) {
    if (n < 30) return "low";
    if (n < 80) return "nominal";
    return "high";
  }
  return "nominal";
}

function normalizeDayNight(raw: string): "D" | "N" {
  const v = raw.toUpperCase().trim();
  return v === "N" ? "N" : "D";
}

function hotspotId(
  lat: number,
  lon: number,
  acqDate: string,
  acqTime: string,
): string {
  const key = `${lat.toFixed(5)}|${lon.toFixed(5)}|${acqDate}|${acqTime}`;
  let hash = 0;
  for (let i = 0; i < key.length; i++) {
    hash = (hash << 5) - hash + key.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash).toString(16).padStart(8, "0");
}
