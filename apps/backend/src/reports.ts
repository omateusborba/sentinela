import type { FireReport } from "@sentinela/shared";
import { parseBbox, type ValidationError } from "./validation";

export const REPORT_DESCRIPTION_MAX = 280;

export type ReportSeverity = FireReport["severity"];

const ALLOWED_SEVERITIES: ReportSeverity[] = ["low", "medium", "high"];

export interface CreateReportBody {
  latitude: number;
  longitude: number;
  description: string;
  severity: ReportSeverity;
}

/** Remove tags HTML básicas e normaliza espaços. */
export function sanitizeDescription(raw: string): string {
  return raw
    .replace(/<[^>]*>/g, "")
    .replace(/\s+/g, " ")
    .trim();
}

export function parseLatitude(raw: unknown): number | ValidationError {
  const latitude = Number(raw);
  if (!Number.isFinite(latitude) || latitude < -90 || latitude > 90) {
    return { error: "latitude must be a number between -90 and 90" };
  }
  return latitude;
}

export function parseLongitude(raw: unknown): number | ValidationError {
  const longitude = Number(raw);
  if (!Number.isFinite(longitude) || longitude < -180 || longitude > 180) {
    return { error: "longitude must be a number between -180 and 180" };
  }
  return longitude;
}

export function parseSeverity(raw: unknown): ReportSeverity | ValidationError {
  if (typeof raw !== "string") {
    return { error: "severity must be one of: low, medium, high" };
  }
  const severity = raw.trim().toLowerCase() as ReportSeverity;
  if (!ALLOWED_SEVERITIES.includes(severity)) {
    return { error: "severity must be one of: low, medium, high" };
  }
  return severity;
}

export function parseCreateReportBody(
  body: unknown,
): CreateReportBody | ValidationError {
  if (typeof body !== "object" || body === null) {
    return { error: "request body must be JSON object" };
  }

  const record = body as Record<string, unknown>;
  const latitude = parseLatitude(record.latitude);
  if ("error" in latitude) return latitude;

  const longitude = parseLongitude(record.longitude);
  if ("error" in longitude) return longitude;

  const severity = parseSeverity(record.severity);
  if ("error" in severity) return severity;

  if (typeof record.description !== "string") {
    return { error: "description must be a non-empty string" };
  }

  const description = sanitizeDescription(record.description);
  if (!description) {
    return { error: "description must not be empty" };
  }
  if (description.length > REPORT_DESCRIPTION_MAX) {
    return {
      error: `description must be at most ${REPORT_DESCRIPTION_MAX} characters`,
    };
  }

  return { latitude, longitude, description, severity };
}

export function parseSince(raw: string | undefined): string | ValidationError {
  if (!raw?.trim()) return "";
  const date = new Date(raw);
  if (Number.isNaN(date.getTime())) {
    return { error: "since must be a valid ISO 8601 date-time" };
  }
  return date.toISOString();
}

export interface ReportsQuery {
  bbox: string;
  west: number;
  south: number;
  east: number;
  north: number;
  since: string;
}

export function parseReportsQuery(
  searchParams: URLSearchParams | Record<string, string>,
): ReportsQuery | ValidationError {
  const getParam = (key: string): string | undefined => {
    if (searchParams instanceof URLSearchParams) {
      return searchParams.get(key) ?? undefined;
    }
    const value = searchParams[key];
    return value === undefined ? undefined : String(value);
  };

  const bboxResult = parseBbox(getParam("bbox"));
  if ("error" in bboxResult) return bboxResult;

  const sinceResult = parseSince(getParam("since"));
  if (typeof sinceResult === "object") return sinceResult;

  const [west, south, east, north] = bboxResult;
  return {
    bbox: `${west},${south},${east},${north}`,
    west,
    south,
    east,
    north,
    since: sinceResult,
  };
}

export function rowToFireReport(row: {
  id: string;
  latitude: number;
  longitude: number;
  description: string;
  severity: string;
  created_at: string;
}): FireReport {
  return {
    id: row.id,
    latitude: row.latitude,
    longitude: row.longitude,
    description: row.description,
    severity: row.severity as ReportSeverity,
    createdAt: row.created_at,
  };
}
