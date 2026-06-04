import type { FireHotspot } from "@sentinela/shared";
import type { FirmsSource } from "@sentinela/shared";

export const CACHE_TTL_SECONDS = 600;

export interface CachedFiresPayload {
  hotspots: FireHotspot[];
  cachedAt: string;
}

export function cacheKey(
  bbox: string,
  days: number,
  source: FirmsSource,
): string {
  return `fires:${source}:${days}:${bbox}`;
}

export async function getCachedFires(
  kv: KVNamespace,
  key: string,
): Promise<CachedFiresPayload | null> {
  const raw = await kv.get(key, "text");
  if (!raw) return null;
  try {
    const parsed = JSON.parse(raw) as CachedFiresPayload;
    if (!Array.isArray(parsed.hotspots)) return null;
    return parsed;
  } catch {
    return null;
  }
}

export async function setCachedFires(
  kv: KVNamespace,
  key: string,
  hotspots: FireHotspot[],
): Promise<void> {
  const payload: CachedFiresPayload = {
    hotspots,
    cachedAt: new Date().toISOString(),
  };
  await kv.put(key, JSON.stringify(payload), {
    expirationTtl: CACHE_TTL_SECONDS,
  });
}
