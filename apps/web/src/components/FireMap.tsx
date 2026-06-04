import { useMemo } from "react";
import { MapContainer, TileLayer, CircleMarker, Popup } from "react-leaflet";
import type { FireHotspot } from "@sentinela/shared";
import { markerColor, markerRadius } from "../utils/markerStyle";
import "leaflet/dist/leaflet.css";

const BRAZIL_CENTER: [number, number] = [-14.5, -52];
const DEFAULT_ZOOM = 4;

interface FireMapProps {
  hotspots: FireHotspot[];
}

function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString("pt-BR", {
    timeZone: "UTC",
    dateStyle: "short",
    timeStyle: "short",
  });
}

export function FireMap({ hotspots }: FireMapProps) {
  const markers = useMemo(() => hotspots, [hotspots]);

  return (
    <section className="map-section" aria-labelledby="map-heading">
      <h2 id="map-heading" className="section-title">
        Mapa de focos
      </h2>
      <div className="map-section__legend" aria-hidden>
        <span>
          <i className="dot dot--low" /> Baixa
        </span>
        <span>
          <i className="dot dot--nominal" /> Nominal
        </span>
        <span>
          <i className="dot dot--high" /> Alta
        </span>
      </div>
      <div className="map-container">
        <MapContainer
          center={BRAZIL_CENTER}
          zoom={DEFAULT_ZOOM}
          scrollWheelZoom
          className="fire-map"
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          {markers.map((fire) => (
            <CircleMarker
              key={fire.id}
              center={[fire.latitude, fire.longitude]}
              radius={markerRadius(fire)}
              pathOptions={{
                color: markerColor(fire),
                fillColor: markerColor(fire),
                fillOpacity: 0.75,
                weight: 1,
              }}
            >
              <Popup>
                <strong>Foco {fire.id}</strong>
                <br />
                {fire.latitude.toFixed(4)}, {fire.longitude.toFixed(4)}
                <br />
                {formatDateTime(fire.acquiredAt)} UTC
                <br />
                {fire.satellite} / {fire.instrument}
                <br />
                Confiança: {fire.confidence}
                {fire.frp !== null && (
                  <>
                    <br />
                    FRP: {fire.frp.toFixed(1)} MW
                  </>
                )}
              </Popup>
            </CircleMarker>
          ))}
        </MapContainer>
      </div>
    </section>
  );
}
