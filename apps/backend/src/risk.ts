import type { FireHotspot, RegionRisk, RiskLevel } from "@sentinela/shared";

/** Score below this → LOW risk. */
export const RISK_THRESHOLD_LOW = 25;
/** Score up to this (inclusive band start for MEDIUM) → MEDIUM; above → HIGH. */
export const RISK_THRESHOLD_HIGH = 60;

const MS_24H = 24 * 60 * 60 * 1000;

const CONFIDENCE_WEIGHT: Record<FireHotspot["confidence"], number> = {
  low: 0.6,
  nominal: 1,
  high: 1.4,
};

export function computeRegionRisk(hotspots: FireHotspot[]): RegionRisk {
  const now = Date.now();
  const totalFires = hotspots.length;

  let weightedSum = 0;
  let maxWeight = 0;

  for (const fire of hotspots) {
    const acquiredMs = Date.parse(fire.acquiredAt);
    const ageHours = Number.isFinite(acquiredMs)
      ? Math.max(0, (now - acquiredMs) / (60 * 60 * 1000))
      : 168;

    const recencyWeight = ageHours <= 24 ? 1.5 : ageHours <= 72 ? 1 : 0.5;
    const frpWeight = fire.frp !== null ? 1 + Math.min(fire.frp / 50, 2) : 1;
    const confWeight = CONFIDENCE_WEIGHT[fire.confidence];
    const weight = recencyWeight * frpWeight * confWeight;

    weightedSum += weight;
    maxWeight += 1.5 * 3 * 1.4;
  }

  const rawScore =
    totalFires === 0
      ? 0
      : Math.min(100, (weightedSum / Math.max(maxWeight, 1)) * 100 * Math.log10(totalFires + 1));

  const score = Math.round(rawScore * 10) / 10;
  const level = scoreToLevel(score);
  const trend = computeTrend(hotspots, now);

  return {
    level,
    score,
    totalFires,
    trend,
  };
}

function scoreToLevel(score: number): RiskLevel {
  if (score < RISK_THRESHOLD_LOW) return "LOW";
  if (score <= RISK_THRESHOLD_HIGH) return "MEDIUM";
  return "HIGH";
}

function computeTrend(
  hotspots: FireHotspot[],
  now: number,
): RegionRisk["trend"] {
  const last24Start = now - MS_24H;
  const prev24Start = now - 2 * MS_24H;

  let recent = 0;
  let previous = 0;

  for (const fire of hotspots) {
    const t = Date.parse(fire.acquiredAt);
    if (!Number.isFinite(t)) continue;
    if (t >= last24Start) recent++;
    else if (t >= prev24Start && t < last24Start) previous++;
  }

  if (recent > previous * 1.2) return "rising";
  if (recent < previous * 0.8) return "falling";
  return "stable";
}
