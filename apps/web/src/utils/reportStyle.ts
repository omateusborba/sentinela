import type { FireReport } from "@sentinela/shared";

/** Tokens alinhados ao tema — reportes da comunidade (distintos do satélite). */
export const REPORT_COLORS = {
  low: "#2563eb",
  medium: "#7c3aed",
  high: "#9333ea",
} as const;

export const REPORT_LABELS = {
  low: "Baixa",
  medium: "Média",
  high: "Alta",
} as const;

export function reportMarkerColor(report: FireReport): string {
  return REPORT_COLORS[report.severity];
}

export function reportMarkerRadius(severity: FireReport["severity"]): number {
  switch (severity) {
    case "high":
      return 10;
    case "medium":
      return 8;
    default:
      return 7;
  }
}
