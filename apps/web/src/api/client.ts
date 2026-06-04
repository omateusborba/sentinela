import type { FireHotspot, RegionRisk } from "@sentinela/shared";
import { API_BASE } from "../config";

export interface FiresResponse {
  count: number;
  bbox: string;
  days: number;
  source: string;
  hotspots: FireHotspot[];
}

export interface RiskResponse {
  bbox: string;
  days: number;
  risk: RegionRisk;
}

export interface ApiError {
  error: string;
}

async function getJson<T>(path: string): Promise<T> {
  const url = `${API_BASE}${path}`;
  const response = await fetch(url);

  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const body = (await response.json()) as ApiError;
      if (body.error) message = body.error;
    } catch {
      /* ignore parse errors */
    }
    throw new Error(message);
  }

  return response.json() as Promise<T>;
}

export function fetchFires(bbox: string, days: number): Promise<FiresResponse> {
  const params = new URLSearchParams({
    bbox,
    days: String(days),
    source: "VIIRS_NOAA20_NRT",
  });
  return getJson<FiresResponse>(`/api/fires?${params}`);
}

export function fetchRisk(bbox: string, days: number): Promise<RiskResponse> {
  const params = new URLSearchParams({ bbox, days: String(days) });
  return getJson<RiskResponse>(`/api/risk?${params}`);
}
