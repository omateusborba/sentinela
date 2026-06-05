import type { PeriodKey } from "../config";
import { PERIOD_OPTIONS } from "../config";

interface HeaderProps {
  period: PeriodKey;
  onPeriodChange: (period: PeriodKey) => void;
  fireCount: number;
  loading: boolean;
}

export function Header({ period, onPeriodChange, fireCount, loading }: HeaderProps) {
  return (
    <header className="header">
      <div className="header__brand">
        <img
          className="header__logo-img"
          src="/logo-lockup.png"
          alt="Sentinela"
          width={200}
          height={40}
        />
      </div>

      <div className="header__actions">
        <div className="period-filter" role="group" aria-label="Período">
          {PERIOD_OPTIONS.map((opt) => (
            <button
              key={opt.key}
              type="button"
              className={`period-filter__btn${period === opt.key ? " period-filter__btn--active" : ""}`}
              onClick={() => onPeriodChange(opt.key)}
              disabled={loading}
            >
              {opt.label}
            </button>
          ))}
        </div>
        <span className="header__count" aria-live="polite">
          {loading ? "Carregando…" : `${fireCount} focos`}
        </span>
      </div>
    </header>
  );
}
