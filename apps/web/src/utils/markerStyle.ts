import type { FireHotspot } from "@sentinela/shared";

const CONFIDENCE_COLOR: Record<FireHotspot["confidence"], string> = {
  low: "#22c55e",
  nominal: "#f59e0b",
  high: "#ef4444",
};

export function markerColor(fire: FireHotspot): string {
  const base = CONFIDENCE_COLOR[fire.confidence];
  if (fire.frp !== null && fire.frp > 100) return "#b91c1c";
  if (fire.frp !== null && fire.frp > 40) return "#dc2626";
  return base;
}

export function markerRadius(fire: FireHotspot): number {
  if (fire.frp === null) return 5;
  if (fire.frp > 100) return 9;
  if (fire.frp > 40) return 7;
  return 5;
}
