import { useState } from "react";
import type { FireReport, ReportFeedback } from "@sentinela/shared";
import { submitReportFeedback } from "../api/client";
import { REPORT_LABELS } from "../utils/reportStyle";

interface ReportPopupProps {
  report: FireReport;
  formatDateTime: (iso: string) => string;
  onRemoved: (id: string) => void;
  onUpdated: (report: FireReport) => void;
}

const FEEDBACK_OPTIONS: { value: ReportFeedback; label: string; className: string }[] = [
  { value: "controlled", label: "Incêndio controlado", className: "report-popup__btn--ok" },
  { value: "false_alarm", label: "Alarme falso", className: "report-popup__btn--muted" },
  { value: "severe", label: "Incêndio grave", className: "report-popup__btn--severe" },
];

export function ReportPopup({
  report,
  formatDateTime,
  onRemoved,
  onUpdated,
}: ReportPopupProps) {
  const [submitting, setSubmitting] = useState<ReportFeedback | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function handleFeedback(feedback: ReportFeedback) {
    setSubmitting(feedback);
    setError(null);
    try {
      const result = await submitReportFeedback(report.id, feedback);
      if (result.removed) {
        onRemoved(report.id);
      } else if (result.report) {
        onUpdated(result.report);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Não foi possível registrar.");
    } finally {
      setSubmitting(null);
    }
  }

  return (
    <div className="report-popup">
      <strong>Reporte da comunidade</strong>
      <p className="report-popup__meta">
        Severidade: {REPORT_LABELS[report.severity]}
      </p>
      <p className="report-popup__description">{report.description}</p>
      <p className="report-popup__meta">
        {formatDateTime(report.createdAt)} UTC
      </p>

      <div className="report-popup__actions" role="group" aria-label="Avaliar reporte">
        <p className="report-popup__actions-label">O que está acontecendo?</p>
        {FEEDBACK_OPTIONS.map((option) => (
          <button
            key={option.value}
            type="button"
            className={`report-popup__btn ${option.className}`}
            disabled={submitting !== null}
            onClick={() => void handleFeedback(option.value)}
          >
            {submitting === option.value ? "Enviando…" : option.label}
          </button>
        ))}
      </div>

      {error && <p className="report-popup__error">{error}</p>}
    </div>
  );
}
