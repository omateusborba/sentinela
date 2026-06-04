import { useMemo, useState } from "react";
import type { PeriodKey } from "./config";
import { PERIOD_OPTIONS } from "./config";
import { useDashboardData } from "./hooks/useDashboardData";
import { Header } from "./components/Header";
import { FireMap } from "./components/FireMap";
import { RiskCards } from "./components/RiskCards";
import { FireTable } from "./components/FireTable";
import { LoadingOverlay, StatusBanner } from "./components/StatusBanner";

export default function App() {
  const [period, setPeriod] = useState<PeriodKey>("24h");
  const days = useMemo(
    () => PERIOD_OPTIONS.find((p) => p.key === period)?.days ?? 1,
    [period],
  );

  const { hotspots, regionCards, loading, error, reload } =
    useDashboardData(days);

  return (
    <div className="app">
      <Header
        period={period}
        onPeriodChange={setPeriod}
        fireCount={hotspots.length}
        loading={loading}
      />

      <main className="app__main">
        {error && (
          <StatusBanner message={error} onRetry={() => void reload()} />
        )}

        {loading && !hotspots.length ? (
          <LoadingOverlay />
        ) : (
          <>
            <RiskCards cards={regionCards} />
            <div className="dashboard-grid">
              <FireMap hotspots={hotspots} />
              <FireTable hotspots={hotspots} />
            </div>
          </>
        )}
      </main>

      <footer className="footer">
        Dados: NASA FIRMS · Mapa: OpenStreetMap · API Sentinela
      </footer>
    </div>
  );
}
