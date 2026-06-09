import { useCallback, useState } from "react";
import type { FireHotspot } from "@sentinela/shared";
import { haversineKm } from "../utils/haversine";

export const DEFAULT_NEAR_RADIUS_KM = 10;

export interface UserLocation {
  latitude: number;
  longitude: number;
}

export interface NearMeResult {
  nearestFire: FireHotspot | null;
  nearestDistanceKm: number | null;
  firesInRadius: FireHotspot[];
}

export function useNearMe(hotspots: FireHotspot[], radiusKm = DEFAULT_NEAR_RADIUS_KM) {
  const [location, setLocation] = useState<UserLocation | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const locate = useCallback(() => {
    if (!navigator.geolocation) {
      setError("Geolocalização não suportada neste navegador.");
      return;
    }
    setLoading(true);
    setError(null);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLocation({
          latitude: pos.coords.latitude,
          longitude: pos.coords.longitude,
        });
        setLoading(false);
      },
      (err) => {
        setLoading(false);
        setError(
          err.code === err.PERMISSION_DENIED
            ? "Permissão de localização negada."
            : "Não foi possível obter sua localização.",
        );
      },
      { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 },
    );
  }, []);

  const evaluation: NearMeResult | null = location
    ? evaluateNearMe(location, hotspots, radiusKm)
    : null;

  return { location, error, loading, locate, evaluation, radiusKm };
}

export function evaluateNearMe(
  location: UserLocation,
  hotspots: FireHotspot[],
  radiusKm: number,
): NearMeResult {
  let nearestFire: FireHotspot | null = null;
  let nearestDistanceKm: number | null = null;
  const firesInRadius: FireHotspot[] = [];

  for (const fire of hotspots) {
    const distance = haversineKm(
      location.latitude,
      location.longitude,
      fire.latitude,
      fire.longitude,
    );
    if (nearestDistanceKm === null || distance < nearestDistanceKm) {
      nearestDistanceKm = distance;
      nearestFire = fire;
    }
    if (distance <= radiusKm) {
      firesInRadius.push(fire);
    }
  }

  return { nearestFire, nearestDistanceKm, firesInRadius };
}
