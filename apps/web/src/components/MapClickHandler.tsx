import { useMapEvents } from "react-leaflet";

interface MapClickHandlerProps {
  enabled: boolean;
  onClick: (lat: number, lon: number) => void;
}

export function MapClickHandler({ enabled, onClick }: MapClickHandlerProps) {
  useMapEvents({
    click(e) {
      if (enabled) {
        onClick(e.latlng.lat, e.latlng.lng);
      }
    },
  });
  return null;
}
