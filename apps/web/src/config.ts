import { SENTINELA_API_URL } from "@sentinela/shared";

/** Brazil bounding box (west,south,east,north). */
export const BRAZIL_BBOX = "-74,-34,-34,6";

/** Limite de caracteres para reportes colaborativos (alinhado ao backend). */
export const REPORT_DESCRIPTION_MAX = 280;

export const API_BASE =
  import.meta.env.VITE_API_URL?.replace(/\/$/, "") || SENTINELA_API_URL;

export type PeriodKey = "24h" | "3d" | "5d";

export const PERIOD_OPTIONS: { key: PeriodKey; label: string; days: number }[] =
  [
    { key: "24h", label: "24h", days: 1 },
    { key: "3d", label: "3 dias", days: 3 },
    { key: "5d", label: "5 dias", days: 5 },
  ];

export interface MonitorRegion {
  id: string;
  name: string;
  bbox: string;
}

/** Macro-regions for risk cards (approximate bboxes). */
export const MONITOR_REGIONS: MonitorRegion[] = [
  { id: "north", name: "Norte", bbox: "-74,-10,-46,6" },
  { id: "northeast", name: "Nordeste", bbox: "-46,-18,-34,0" },
  { id: "center-west", name: "Centro-Oeste", bbox: "-65,-25,-46,-8" },
  { id: "southeast", name: "Sudeste", bbox: "-53,-25,-39,-15" },
  { id: "south", name: "Sul", bbox: "-58,-35,-48,-22" },
];
