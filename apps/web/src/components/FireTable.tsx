import { useMemo } from "react";
import type { FireHotspot } from "@sentinela/shared";

interface FireTableProps {
  hotspots: FireHotspot[];
  maxRows?: number;
}

function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString("pt-BR", {
    timeZone: "UTC",
    dateStyle: "short",
    timeStyle: "short",
  });
}

const CONFIDENCE_PT: Record<FireHotspot["confidence"], string> = {
  low: "Baixa",
  nominal: "Nominal",
  high: "Alta",
};

export function FireTable({ hotspots, maxRows = 50 }: FireTableProps) {
  const rows = useMemo(
    () =>
      [...hotspots]
        .sort(
          (a, b) =>
            Date.parse(b.acquiredAt) - Date.parse(a.acquiredAt),
        )
        .slice(0, maxRows),
    [hotspots, maxRows],
  );

  return (
    <section className="fire-table-section" aria-labelledby="table-heading">
      <h2 id="table-heading" className="section-title">
        Focos recentes
      </h2>
      <div className="table-wrap">
        <table className="fire-table">
          <thead>
            <tr>
              <th>Data/hora (UTC)</th>
              <th>Latitude</th>
              <th>Longitude</th>
              <th>Sensor</th>
              <th>Confiança</th>
              <th>FRP (MW)</th>
            </tr>
          </thead>
          <tbody>
            {rows.length === 0 ? (
              <tr>
                <td colSpan={6} className="fire-table__empty">
                  Nenhum foco no período selecionado.
                </td>
              </tr>
            ) : (
              rows.map((fire) => (
                <tr key={fire.id}>
                  <td>{formatDateTime(fire.acquiredAt)}</td>
                  <td>{fire.latitude.toFixed(4)}</td>
                  <td>{fire.longitude.toFixed(4)}</td>
                  <td>
                    {fire.satellite}
                    {fire.instrument ? ` / ${fire.instrument}` : ""}
                  </td>
                  <td>
                    <span
                      className={`badge badge--${fire.confidence}`}
                    >
                      {CONFIDENCE_PT[fire.confidence]}
                    </span>
                  </td>
                  <td>{fire.frp !== null ? fire.frp.toFixed(1) : "—"}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      {hotspots.length > maxRows && (
        <p className="fire-table__more">
          Exibindo {maxRows} de {hotspots.length} focos (mais recentes primeiro).
        </p>
      )}
    </section>
  );
}
