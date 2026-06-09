import { useCallback, useEffect, useState } from "react";
import type { FireHotspot, FireReport } from "@sentinela/shared";
import { fetchFires, fetchReports, fetchRisk, type RiskResponse } from "../api/client";
import { BRAZIL_BBOX, MONITOR_REGIONS } from "../config";

export interface RegionRiskCard {
  regionId: string;
  regionName: string;
  data: RiskResponse | null;
  error: string | null;
}

interface DashboardState {
  hotspots: FireHotspot[];
  reports: FireReport[];
  regionCards: RegionRiskCard[];
  loading: boolean;
  error: string | null;
}

export function useDashboardData(days: number) {
  const [state, setState] = useState<DashboardState>({
    hotspots: [],
    reports: [],
    regionCards: MONITOR_REGIONS.map((r) => ({
      regionId: r.id,
      regionName: r.name,
      data: null,
      error: null,
    })),
    loading: true,
    error: null,
  });

  const load = useCallback(async () => {
    setState((prev) => ({ ...prev, loading: true, error: null }));

    try {
      const firesPromise = fetchFires(BRAZIL_BBOX, days);
      const reportsPromise = fetchReports(BRAZIL_BBOX).catch(() => [] as FireReport[]);
      const riskPromises = MONITOR_REGIONS.map(async (region) => {
        try {
          const data = await fetchRisk(region.bbox, days);
          return {
            regionId: region.id,
            regionName: region.name,
            data,
            error: null,
          } satisfies RegionRiskCard;
        } catch (err) {
          const message = err instanceof Error ? err.message : "Erro ao carregar";
          return {
            regionId: region.id,
            regionName: region.name,
            data: null,
            error: message,
          } satisfies RegionRiskCard;
        }
      });

      const [fires, reports, ...regionCards] = await Promise.all([
        firesPromise,
        reportsPromise,
        ...riskPromises,
      ]);

      setState({
        hotspots: fires.hotspots,
        reports,
        regionCards,
        loading: false,
        error: null,
      });
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "Falha ao carregar dados";
      setState((prev) => ({
        ...prev,
        loading: false,
        error: message,
      }));
    }
  }, [days]);

  useEffect(() => {
    void load();
  }, [load]);

  return { ...state, reload: load };
}
