/** Backend Worker em produção (Cloudflare). */
export const SENTINELA_API_URL = "https://sentinela.mateus-borba.workers.dev";

export interface FireHotspot {
  id: string;
  latitude: number;
  longitude: number;
  acquiredAt: string;
  satellite: string;
  instrument: string;
  confidence: "low" | "nominal" | "high";
  frp: number | null;
  dayNight: "D" | "N";
}

export type RiskLevel = "LOW" | "MEDIUM" | "HIGH";

export interface RegionRisk {
  level: RiskLevel;
  score: number;
  totalFires: number;
  trend: "rising" | "stable" | "falling";
}

export const ALLOWED_FIRMS_SOURCES = [
  "VIIRS_NOAA20_NRT",
  "VIIRS_SNPP_NRT",
  "MODIS_NRT",
] as const;

export type FirmsSource = (typeof ALLOWED_FIRMS_SOURCES)[number];

export interface FiresQueryParams {
  bbox: string;
  days: number;
  source: FirmsSource;
}
