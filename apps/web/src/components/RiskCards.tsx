import type { RiskLevel } from "@sentinela/shared";
import type { RegionRiskCard } from "../hooks/useDashboardData";

const LEVEL_CLASS: Record<RiskLevel, string> = {
  LOW: "risk-card--low",
  MEDIUM: "risk-card--medium",
  HIGH: "risk-card--high",
};

const LEVEL_LABEL: Record<RiskLevel, string> = {
  LOW: "Baixo",
  MEDIUM: "Médio",
  HIGH: "Alto",
};

const TREND_LABEL = {
  rising: "↑ Subindo",
  stable: "→ Estável",
  falling: "↓ Caindo",
} as const;

interface RiskCardsProps {
  cards: RegionRiskCard[];
}

export function RiskCards({ cards }: RiskCardsProps) {
  return (
    <section className="risk-cards" aria-labelledby="risk-heading">
      <h2 id="risk-heading" className="section-title">
        Risco por região
      </h2>
      <div className="risk-cards__grid">
        {cards.map((card) => (
          <article
            key={card.regionId}
            className={`risk-card${
              card.data ? ` ${LEVEL_CLASS[card.data.risk.level]}` : ""
            }`}
          >
            <h3 className="risk-card__name">{card.regionName}</h3>
            {card.error && <p className="risk-card__error">{card.error}</p>}
            {card.data && (
              <>
                <p className="risk-card__level">
                  {LEVEL_LABEL[card.data.risk.level]}
                </p>
                <p className="risk-card__score">
                  Score <strong>{card.data.risk.score}</strong>
                  <span className="risk-card__muted"> / 100</span>
                </p>
                <p className="risk-card__meta">
                  {card.data.risk.totalFires} focos ·{" "}
                  {TREND_LABEL[card.data.risk.trend]}
                </p>
              </>
            )}
            {!card.data && !card.error && (
              <p className="risk-card__muted">—</p>
            )}
          </article>
        ))}
      </div>
    </section>
  );
}
