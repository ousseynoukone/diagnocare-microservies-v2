import React, { useEffect, createElement } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import type { Doctor } from './DoctorCard';
import { MapPin } from 'lucide-react';
import L from 'leaflet';
// Fix for default marker icons in Leaflet with React
const iconPerson = new L.Icon({
  iconUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon.png',
  iconRetinaUrl:
  'https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});
interface SpecialistMapProps {
  doctors: Doctor[];
  selectedDoctorId?: string;
  onMarkerClick: (doctorId: string) => void;
  center: [number, number];
}
function MapUpdater({ center }: {center: [number, number];}) {
  const map = useMap();
  useEffect(() => {
    map.setView(center, 13);
  }, [center, map]);
  return null;
}
export function SpecialistMap({
  doctors,
  selectedDoctorId,
  onMarkerClick,
  center
}: SpecialistMapProps) {
  // Inject Leaflet CSS
  useEffect(() => {
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
    link.integrity = 'sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=';
    link.crossOrigin = '';
    document.head.appendChild(link);
    return () => {
      document.head.removeChild(link);
    };
  }, []);
  return (
    <div className="h-full w-full rounded-xl overflow-hidden border border-slate-200 shadow-inner bg-slate-100 relative z-0">
      <MapContainer
        center={center}
        zoom={13}
        style={{
          height: '100%',
          width: '100%'
        }}
        scrollWheelZoom={true}>

        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />

        <MapUpdater center={center} />

        {doctors.map((doctor) =>
        <Marker
          key={doctor.id}
          position={doctor.coordinates}
          icon={iconPerson}
          eventHandlers={{
            click: () => onMarkerClick(doctor.id)
          }}
          opacity={
          selectedDoctorId && selectedDoctorId !== doctor.id ? 0.6 : 1
          }>

            <Popup>
              <div className="p-1">
                <h3 className="font-bold text-sm">{doctor.name}</h3>
                <p className="text-xs text-slate-600">{doctor.specialty}</p>
                <p className="text-xs font-medium text-blue-600 mt-1">
                  {doctor.nextAvailability}
                </p>
              </div>
            </Popup>
          </Marker>
        )}
      </MapContainer>
    </div>);

}