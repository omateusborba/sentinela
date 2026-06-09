import { useEffect } from "react";
import { useMap } from "react-leaflet";

interface MapFlyToProps {
  center: [number, number] | null;
  zoom?: number;
}

export function MapFlyTo({ center, zoom = 10 }: MapFlyToProps) {
  const map = useMap();

  useEffect(() => {
    if (center) {
      map.flyTo(center, zoom, { duration: 0.8 });
    }
  }, [center, zoom, map]);

  return null;
}
