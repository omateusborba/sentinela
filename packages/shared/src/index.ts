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

/** Reporte colaborativo de incêndio (persistido no D1). */
export interface FireReport {
  id: string;
  latitude: number;
  longitude: number;
  description: string;
  severity: "low" | "medium" | "high";
  createdAt: string; // ISO 8601
}

export interface CreateFireReportBody {
  latitude: number;
  longitude: number;
  description: string;
  severity: FireReport["severity"];
}

/** Feedback da comunidade sobre um reporte ativo. */
export type ReportFeedback = "controlled" | "false_alarm" | "severe";

export interface ReportFeedbackBody {
  feedback: ReportFeedback;
}

export interface ReportFeedbackResponse {
  id: string;
  removed: boolean;
  report?: FireReport;
}

/** Reportes ativos expiram após este período (horas). */
export const REPORT_TTL_HOURS = 24;
