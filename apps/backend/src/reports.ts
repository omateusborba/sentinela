import type { FireReport, ReportFeedback } from "@sentinela/shared";
import { REPORT_TTL_HOURS } from "@sentinela/shared";
import { isValidationError, parseBbox, type ValidationError } from "./validation";

export const REPORT_DESCRIPTION_MAX = 280;
export const REPORT_ACTIVE_STATUS = "active";
export const REPORT_CONTROLLED_STATUS = "controlled";
export const REPORT_FALSE_ALARM_STATUS = "false_alarm";

export type ReportSeverity = FireReport["severity"];

const ALLOWED_SEVERITIES: ReportSeverity[] = ["low", "medium", "high"];
const ALLOWED_FEEDBACK: ReportFeedback[] = [
  "controlled",
  "false_alarm",
  "severe",
];

export interface CreateReportBody {
  latitude: number;
  longitude: number;
  description: string;
  severity: ReportSeverity;
}

/** Remove tags HTML básicas e normaliza espaços (preserva quebras de linha). */
export function sanitizeDescription(raw: string): string {
  return raw
    .replace(/<[^>]*>/g, "")
    .replace(/[^\S\n]+/g, " ")
    .replace(/\n{3,}/g, "\n\n")
    .trim();
}

export function defaultReportSince(): string {
  return new Date(
    Date.now() - REPORT_TTL_HOURS * 60 * 60 * 1000,
  ).toISOString();
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
  if (isValidationError(latitude)) return latitude;

  const longitude = parseLongitude(record.longitude);
  if (isValidationError(longitude)) return longitude;

  const severity = parseSeverity(record.severity);
  if (isValidationError(severity)) return severity;

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
  if (!raw?.trim()) return defaultReportSince();
  const date = new Date(raw);
  if (Number.isNaN(date.getTime())) {
    return { error: "since must be a valid ISO 8601 date-time" };
  }
  return date.toISOString();
}

export function parseReportFeedback(
  body: unknown,
): ReportFeedback | ValidationError {
  if (typeof body !== "object" || body === null) {
    return { error: "request body must be JSON object" };
  }
  const record = body as Record<string, unknown>;
  if (typeof record.feedback !== "string") {
    return {
      error: "feedback must be one of: controlled, false_alarm, severe",
    };
  }
  const feedback = record.feedback.trim().toLowerCase() as ReportFeedback;
  if (!ALLOWED_FEEDBACK.includes(feedback)) {
    return {
      error: "feedback must be one of: controlled, false_alarm, severe",
    };
  }
  return feedback;
}

export function feedbackToStatus(feedback: ReportFeedback): string | null {
  switch (feedback) {
    case "controlled":
      return REPORT_CONTROLLED_STATUS;
    case "false_alarm":
      return REPORT_FALSE_ALARM_STATUS;
    default:
      return null;
  }
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
  if (isValidationError(bboxResult)) return bboxResult;

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
