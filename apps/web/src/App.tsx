import { useMemo, useState } from "react";
import type { FireReport } from "@sentinela/shared";
import type { PeriodKey } from "./config";
import { PERIOD_OPTIONS } from "./config";
import { useDashboardData } from "./hooks/useDashboardData";
import { useNearMe } from "./hooks/useNearMe";
import { Header } from "./components/Header";
import { FireMap } from "./components/FireMap";
import { RiskCards } from "./components/RiskCards";
import { FireTable } from "./components/FireTable";
import { NearMePanel } from "./components/NearMePanel";
import { ReportFireDialog } from "./components/ReportFireDialog";
import { LoadingOverlay, StatusBanner } from "./components/StatusBanner";

export default function App() {
  const [period, setPeriod] = useState<PeriodKey>("24h");
  const days = useMemo(
    () => PERIOD_OPTIONS.find((p) => p.key === period)?.days ?? 1,
    [period],
  );

  const { hotspots, reports, regionCards, loading, error, reload } =
    useDashboardData(days);

  const { location, error: locError, loading: locLoading, evaluation, radiusKm } =
    useNearMe(hotspots);

  const [reportOpen, setReportOpen] = useState(false);
  const [mapPickMode, setMapPickMode] = useState(false);
  const [pickCoords, setPickCoords] = useState<{ lat: number; lon: number } | null>(
    null,
  );
  const [localReports, setLocalReports] = useState<FireReport[]>([]);

  const allReports = useMemo(
    () => [...reports, ...localReports.filter((r) => !reports.some((x) => x.id === r.id))],
    [reports, localReports],
  );

  const flyToCenter: [number, number] | null = location
    ? [location.latitude, location.longitude]
    : null;

  function openReportDialog() {
    setPickCoords(
      location
        ? { lat: location.latitude, lon: location.longitude }
        : null,
    );
    setReportOpen(true);
  }

  function handleMapPick(lat: number, lon: number) {
    setPickCoords({ lat, lon });
    setMapPickMode(false);
    setReportOpen(true);
  }

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
            <NearMePanel
              loading={locLoading}
              error={locError}
              location={location}
              evaluation={evaluation}
              radiusKm={radiusKm}
            />
            <div className="map-actions">
              <button
                type="button"
                className="btn btn--primary"
                onClick={openReportDialog}
              >
                Reportar incêndio
              </button>
              <button
                type="button"
                className="btn btn--ghost"
                onClick={() => {
                  setMapPickMode(true);
                  setReportOpen(false);
                }}
              >
                Escolher no mapa
              </button>
            </div>
            <div className="dashboard-grid">
              <FireMap
                hotspots={hotspots}
                reports={allReports}
                userLocation={location}
                flyToCenter={flyToCenter}
                mapPickMode={mapPickMode}
                onMapPick={handleMapPick}
              />
              <FireTable hotspots={hotspots} />
            </div>
          </>
        )}
      </main>

      <footer className="footer">
        Dados: NASA FIRMS · Mapa: OpenStreetMap · API Sentinela · Reportes colaborativos (MVP anônimo)
      </footer>

      <ReportFireDialog
        open={reportOpen}
        initialLat={pickCoords?.lat ?? location?.latitude ?? null}
        initialLon={pickCoords?.lon ?? location?.longitude ?? null}
        onClose={() => setReportOpen(false)}
        onSubmitted={(report) => {
          setLocalReports((prev) => [report, ...prev]);
          void reload();
        }}
      />
    </div>
  );
}
