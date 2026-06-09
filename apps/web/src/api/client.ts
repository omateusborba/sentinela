import type {
  CreateFireReportBody,
  FireHotspot,
  FireReport,
  RegionRisk,
  ReportFeedback,
  ReportFeedbackResponse,
} from "@sentinela/shared";
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

async function postJson<T>(path: string, body: unknown): Promise<T> {
  const url = `${API_BASE}${path}`;
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const payload = (await response.json()) as ApiError;
      if (payload.error) message = payload.error;
    } catch {
      /* ignore */
    }
    throw new Error(message);
  }

  return response.json() as Promise<T>;
}

export function fetchReports(bbox: string, since?: string): Promise<FireReport[]> {
  const params = new URLSearchParams({ bbox });
  if (since) params.set("since", since);
  return getJson<FireReport[]>(`/api/reports?${params}`);
}

export function submitReport(body: CreateFireReportBody): Promise<FireReport> {
  return postJson<FireReport>("/api/reports", body);
}

export function submitReportFeedback(
  id: string,
  feedback: ReportFeedback,
): Promise<ReportFeedbackResponse> {
  return postJson<ReportFeedbackResponse>(`/api/reports/${id}/feedback`, {
    feedback,
  });
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
