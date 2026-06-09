import type { NearMeResult, UserLocation } from "../hooks/useNearMe";

interface NearMePanelProps {
  loading: boolean;
  error: string | null;
  location: UserLocation | null;
  evaluation: NearMeResult | null;
  radiusKm: number;
}

export function NearMePanel({
  loading,
  error,
  location,
  evaluation,
  radiusKm,
}: NearMePanelProps) {
  const inAlert = (evaluation?.firesInRadius.length ?? 0) > 0;

  return (
    <section className="near-me" aria-labelledby="near-me-heading">
      <h2 id="near-me-heading" className="section-title">
        Perto de mim
      </h2>
      <div className={`near-me__card${inAlert ? " near-me__card--alert" : ""}`}>
        {loading && !location && (
          <p className="near-me__loading">Obtendo localização…</p>
        )}

        {error && <p className="near-me__error">{error}</p>}

        {location && evaluation && (
          <div className="near-me__result">
            <p className="near-me__coords">
              Você: {location.latitude.toFixed(4)}, {location.longitude.toFixed(4)}
            </p>
            {evaluation.nearestFire && evaluation.nearestDistanceKm !== null ? (
              <>
                <p>
                  Foco mais próximo:{" "}
                  <strong>{evaluation.nearestDistanceKm.toFixed(1)} km</strong>
                </p>
                {inAlert ? (
                  <p className="near-me__alert" role="alert">
                    ⚠ {evaluation.firesInRadius.length} foco(s) dentro de {radiusKm} km
                  </p>
                ) : (
                  <p className="near-me__ok">
                    Nenhum foco de satélite dentro de {radiusKm} km.
                  </p>
                )}
              </>
            ) : (
              <p className="near-me__ok">Nenhum foco no período selecionado.</p>
            )}
          </div>
        )}
      </div>
    </section>
  );
}
