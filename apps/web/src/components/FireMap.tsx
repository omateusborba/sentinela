import { useMemo } from "react";
import {
  MapContainer,
  TileLayer,
  CircleMarker,
  Popup,
  Circle,
} from "react-leaflet";
import type { FireHotspot, FireReport } from "@sentinela/shared";
import { markerColor, markerRadius } from "../utils/markerStyle";
import {
  reportMarkerColor,
  reportMarkerRadius,
  REPORT_LABELS,
} from "../utils/reportStyle";
import { DEFAULT_NEAR_RADIUS_KM, type UserLocation } from "../hooks/useNearMe";
import { MapResizeHandler } from "./MapResizeHandler";
import { MapFlyTo } from "./MapFlyTo";
import { MapClickHandler } from "./MapClickHandler";
import { ReportPopup } from "./ReportPopup";
import "leaflet/dist/leaflet.css";

const BRAZIL_CENTER: [number, number] = [-14.5, -52];
const DEFAULT_ZOOM = 4;

interface FireMapProps {
  hotspots: FireHotspot[];
  reports: FireReport[];
  userLocation: UserLocation | null;
  flyToCenter: [number, number] | null;
  mapPickMode?: boolean;
  onMapPick?: (lat: number, lon: number) => void;
  onReportRemoved?: (id: string) => void;
  onReportUpdated?: (report: FireReport) => void;
}

function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString("pt-BR", {
    timeZone: "UTC",
    dateStyle: "short",
    timeStyle: "short",
  });
}

export function FireMap({
  hotspots,
  reports,
  userLocation,
  flyToCenter,
  mapPickMode = false,
  onMapPick,
  onReportRemoved,
  onReportUpdated,
}: FireMapProps) {
  const markers = useMemo(() => hotspots, [hotspots]);

  return (
    <section className="map-section" aria-labelledby="map-heading">
      <div className="map-section__header">
        <h2 id="map-heading" className="section-title">
          Mapa de focos
        </h2>
        {mapPickMode && (
          <p className="map-section__pick-hint">Clique no mapa para definir coordenadas</p>
        )}
      </div>
      <div className="map-section__legend" aria-label="Legenda do mapa">
        <span className="map-legend__group">
          <strong>Satélite:</strong>
          <span><i className="dot dot--low" /> Baixa</span>
          <span><i className="dot dot--nominal" /> Nominal</span>
          <span><i className="dot dot--high" /> Alta</span>
        </span>
        <span className="map-legend__group">
          <strong>Comunidade:</strong>
          <span><i className="dot dot--report-low" /> {REPORT_LABELS.low}</span>
          <span><i className="dot dot--report-medium" /> {REPORT_LABELS.medium}</span>
          <span><i className="dot dot--report-high" /> {REPORT_LABELS.high}</span>
        </span>
        <span className="map-legend__group">
          <span><i className="dot dot--user" /> Você</span>
        </span>
      </div>
      <div className="map-container">
        <MapContainer
          center={BRAZIL_CENTER}
          zoom={DEFAULT_ZOOM}
          scrollWheelZoom
          className="fire-map"
        >
          <MapResizeHandler />
          <MapFlyTo center={flyToCenter} />
          {onMapPick && (
            <MapClickHandler enabled={mapPickMode} onClick={onMapPick} />
          )}
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          {userLocation && (
            <>
              <Circle
                center={[userLocation.latitude, userLocation.longitude]}
                radius={DEFAULT_NEAR_RADIUS_KM * 1000}
                pathOptions={{
                  color: "#2563eb",
                  fillColor: "#2563eb",
                  fillOpacity: 0.08,
                  weight: 1,
                  dashArray: "6 4",
                }}
              />
              <CircleMarker
                center={[userLocation.latitude, userLocation.longitude]}
                radius={9}
                pathOptions={{
                  color: "#ffffff",
                  fillColor: "#2563eb",
                  fillOpacity: 1,
                  weight: 3,
                }}
              >
                <Popup>
                  <strong>Sua localização</strong>
                </Popup>
              </CircleMarker>
            </>
          )}
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
                <strong>Foco satélite {fire.id}</strong>
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
          {reports.map((report) => (
            <CircleMarker
              key={report.id}
              center={[report.latitude, report.longitude]}
              radius={reportMarkerRadius(report.severity)}
              pathOptions={{
                color: "#ffffff",
                fillColor: reportMarkerColor(report),
                fillOpacity: 0.9,
                weight: 2,
                dashArray: "2 2",
              }}
            >
              <Popup maxWidth={300} minWidth={220}>
                <ReportPopup
                  report={report}
                  formatDateTime={formatDateTime}
                  onRemoved={(id) => onReportRemoved?.(id)}
                  onUpdated={(updated) => onReportUpdated?.(updated)}
                />
              </Popup>
            </CircleMarker>
          ))}
        </MapContainer>
      </div>
    </section>
  );
}
