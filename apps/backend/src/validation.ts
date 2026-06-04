import {
  ALLOWED_FIRMS_SOURCES,
  type FirmsSource,
  type FiresQueryParams,
} from "@sentinela/shared";

const DEFAULT_SOURCE: FirmsSource = "VIIRS_NOAA20_NRT";

export interface ValidationError {
  error: string;
}

export function parseBbox(raw: string | undefined): number[] | ValidationError {
  if (!raw?.trim()) {
    return { error: "bbox is required (west,south,east,north)" };
  }

  const parts = raw.split(",").map((p) => p.trim());
  if (parts.length !== 4) {
    return { error: "bbox must have 4 comma-separated values: west,south,east,north" };
  }

  const nums = parts.map((p) => Number(p));
  if (nums.some((n) => !Number.isFinite(n))) {
    return { error: "bbox values must be valid numbers" };
  }

  const [west, south, east, north] = nums;
  if (west >= east || south >= north) {
    return { error: "bbox invalid: west < east and south < north required" };
  }
  if (west < -180 || east > 180 || south < -90 || north > 90) {
    return { error: "bbox coordinates out of range" };
  }

  return nums;
}

export function parseDays(
  raw: string | undefined,
  defaultDays = 1,
): number | ValidationError {
  if (raw === undefined || raw === "") return defaultDays;
  const days = Number(raw);
  if (!Number.isInteger(days) || days < 1 || days > 5) {
    return { error: "days must be an integer between 1 and 5 (FIRMS limit for this source)" };
  }
  return days;
}

export function parseSource(raw: string | undefined): FirmsSource | ValidationError {
  const source = (raw?.trim() || DEFAULT_SOURCE) as FirmsSource;
  if (!ALLOWED_FIRMS_SOURCES.includes(source)) {
    return {
      error: `source must be one of: ${ALLOWED_FIRMS_SOURCES.join(", ")}`,
    };
  }
  return source;
}

function getParam(
  params: URLSearchParams | Record<string, string>,
  key: string,
): string | undefined {
  if (params instanceof URLSearchParams) {
    return params.get(key) ?? undefined;
  }
  const value = params[key];
  return value === undefined ? undefined : String(value);
}

export function parseFiresQuery(
  searchParams: URLSearchParams | Record<string, string>,
): FiresQueryParams | ValidationError {
  const bboxResult = parseBbox(getParam(searchParams, "bbox"));
  if ("error" in bboxResult) return bboxResult;

  const daysResult = parseDays(getParam(searchParams, "days"), 1);
  if (typeof daysResult === "object") return daysResult;

  const sourceResult = parseSource(getParam(searchParams, "source"));
  if (typeof sourceResult === "object") return sourceResult;

  const [west, south, east, north] = bboxResult;
  return {
    bbox: `${west},${south},${east},${north}`,
    days: daysResult,
    source: sourceResult,
  };
}

export function isValidationError(
  value: unknown,
): value is ValidationError {
  return (
    typeof value === "object" &&
    value !== null &&
    "error" in value &&
    typeof (value as ValidationError).error === "string"
  );
}
