import { useEffect, useState } from "react";
import type { CreateFireReportBody, FireReport } from "@sentinela/shared";
import { submitReport } from "../api/client";
import { REPORT_DESCRIPTION_MAX } from "../config";
import { REPORT_LABELS } from "../utils/reportStyle";

interface ReportFireDialogProps {
  open: boolean;
  initialLat: number | null;
  initialLon: number | null;
  onClose: () => void;
  onSubmitted: (report: FireReport) => void;
}

export function ReportFireDialog({
  open,
  initialLat,
  initialLon,
  onClose,
  onSubmitted,
}: ReportFireDialogProps) {
  const [latitude, setLatitude] = useState("");
  const [longitude, setLongitude] = useState("");
  const [description, setDescription] = useState("");
  const [severity, setSeverity] = useState<FireReport["severity"]>("medium");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (!open) return;
    setLatitude(initialLat !== null ? String(initialLat) : "");
    setLongitude(initialLon !== null ? String(initialLon) : "");
    setDescription("");
    setSeverity("medium");
    setError(null);
    setSuccess(false);
  }, [open, initialLat, initialLon]);

  if (!open) return null;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    const body: CreateFireReportBody = {
      latitude: Number(latitude),
      longitude: Number(longitude),
      description: description.trim(),
      severity,
    };
    try {
      const report = await submitReport(body);
      setSuccess(true);
      onSubmitted(report);
      setTimeout(onClose, 1200);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Falha ao enviar reporte");
    } finally {
      setLoading(false);
    }
  }

  function useMyLocation() {
    if (!navigator.geolocation) {
      setError("Geolocalização indisponível.");
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLatitude(String(pos.coords.latitude));
        setLongitude(String(pos.coords.longitude));
        setError(null);
      },
      () => setError("Permissão de localização negada."),
      { enableHighAccuracy: true },
    );
  }

  const remaining = REPORT_DESCRIPTION_MAX - description.length;

  return (
    <div className="modal-backdrop" role="presentation" onClick={onClose}>
      <div
        className="modal"
        role="dialog"
        aria-labelledby="report-title"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 id="report-title" className="modal__title">
          Reportar incêndio
        </h2>
        <p className="modal__hint">
          Reporte anônimo da comunidade — use para acionar órgãos competentes.
        </p>
        <form className="report-form" onSubmit={(e) => void handleSubmit(e)}>
          <label className="report-form__label">
            Latitude
            <input
              type="number"
              step="any"
              required
              value={latitude}
              onChange={(e) => setLatitude(e.target.value)}
            />
          </label>
          <label className="report-form__label">
            Longitude
            <input
              type="number"
              step="any"
              required
              value={longitude}
              onChange={(e) => setLongitude(e.target.value)}
            />
          </label>
          <button
            type="button"
            className="btn btn--ghost report-form__geo"
            onClick={useMyLocation}
          >
            Usar minha localização
          </button>
          <label className="report-form__label">
            Descrição
            <textarea
              required
              maxLength={REPORT_DESCRIPTION_MAX}
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
            <span className="report-form__counter">{remaining} caracteres restantes</span>
          </label>
          <fieldset className="report-form__severity">
            <legend>Severidade</legend>
            {(["low", "medium", "high"] as const).map((level) => (
              <label key={level} className="report-form__radio">
                <input
                  type="radio"
                  name="severity"
                  checked={severity === level}
                  onChange={() => setSeverity(level)}
                />
                {REPORT_LABELS[level]}
              </label>
            ))}
          </fieldset>
          {error && <p className="report-form__error">{error}</p>}
          {success && <p className="report-form__success">Reporte enviado!</p>}
          <div className="modal__actions">
            <button type="button" className="btn btn--ghost" onClick={onClose}>
              Cancelar
            </button>
            <button type="submit" className="btn btn--primary" disabled={loading}>
              {loading ? "Enviando…" : "Enviar reporte"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
