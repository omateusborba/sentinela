import { useEffect } from "react";
import { useMap } from "react-leaflet";

/** Recalcula o Leaflet quando o container muda de tamanho (layout responsivo). */
export function MapResizeHandler() {
  const map = useMap();

  useEffect(() => {
    const container = map.getContainer().parentElement;
    if (!container) return;

    const observer = new ResizeObserver(() => {
      map.invalidateSize();
    });
    observer.observe(container);
    return () => observer.disconnect();
  }, [map]);

  return null;
}
