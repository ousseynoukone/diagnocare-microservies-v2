import React, { useState } from 'react';
import {
  ArrowLeft,
  Search,
  Map as MapIcon,
  List,
  Filter,
  Navigation as NavIcon } from
'lucide-react';
import { Button } from '../components/ui/Button';
import { DoctorCard } from '../components/DoctorCard';
import type { Doctor } from '../components/DoctorCard';
import { SpecialistMap } from '../components/SpecialistMap';
import { Card } from '../components/ui/Card';
interface SpecialistFinderPageProps {
  onBack: () => void;
  initialSpecialty?: string;
}
// Mock data generator
const MOCK_DOCTORS: Doctor[] = [
{
  id: '1',
  name: 'Dr. Sophie Martin',
  specialty: 'Neurologue',
  address: '15 Rue de la République, 75001 Paris',
  distance: '0.8 km',
  rating: 4.9,
  reviewCount: 124,
  nextAvailability: "Aujourd'hui à 16:30",
  imageUrl:
  'https://images.unsplash.com/photo-1559839734-2b71ea197ec2?auto=format&fit=crop&q=80&w=300&h=300',
  isConventionne: true,
  coordinates: [48.8606, 2.3376]
},
{
  id: '2',
  name: 'Dr. Thomas Dubois',
  specialty: 'Neurologue',
  address: '42 Boulevard Saint-Germain, 75005 Paris',
  distance: '1.2 km',
  rating: 4.7,
  reviewCount: 89,
  nextAvailability: 'Demain à 09:00',
  imageUrl:
  'https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?auto=format&fit=crop&q=80&w=300&h=300',
  isConventionne: true,
  coordinates: [48.8506, 2.3476]
},
{
  id: '3',
  name: 'Dr. Marie Laurent',
  specialty: 'Neurologue',
  address: '8 Avenue des Champs-Élysées, 75008 Paris',
  distance: '2.5 km',
  rating: 4.8,
  reviewCount: 215,
  nextAvailability: 'Jeudi 14 mars',
  imageUrl:
  'https://images.unsplash.com/photo-1594824476967-48c8b964273f?auto=format&fit=crop&q=80&w=300&h=300',
  isConventionne: false,
  coordinates: [48.8698, 2.3075]
},
{
  id: '4',
  name: 'Dr. Jean-Michel Bernard',
  specialty: 'Neurologue',
  address: '128 Rue de Rivoli, 75001 Paris',
  distance: '1.5 km',
  rating: 4.5,
  reviewCount: 56,
  nextAvailability: 'Vendredi 15 mars',
  imageUrl:
  'https://images.unsplash.com/photo-1537368910025-700350fe46c7?auto=format&fit=crop&q=80&w=300&h=300',
  isConventionne: true,
  coordinates: [48.859, 2.342]
},
{
  id: '5',
  name: 'Centre Neurologique Paris-Est',
  specialty: 'Centre Médical',
  address: '25 Rue du Faubourg Saint-Antoine, 75011 Paris',
  distance: '3.1 km',
  rating: 4.6,
  reviewCount: 342,
  nextAvailability: "Aujourd'hui à 17:45",
  imageUrl:
  'https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?auto=format&fit=crop&q=80&w=300&h=300',
  isConventionne: true,
  coordinates: [48.851, 2.372]
}];

export function SpecialistFinderPage({
  onBack,
  initialSpecialty = 'Neurologue'
}: SpecialistFinderPageProps) {
  const [viewMode, setViewMode] = useState<'list' | 'map'>('list');
  const [searchQuery, setSearchQuery] = useState(initialSpecialty);
  const [selectedDoctorId, setSelectedDoctorId] = useState<string | undefined>();
  const [userLocation, setUserLocation] = useState<string>('Paris, 75001');
  // In a real app, we would filter based on search query and map bounds
  const filteredDoctors = MOCK_DOCTORS.filter(
    (d) =>
    d.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    d.specialty.toLowerCase().includes(searchQuery.toLowerCase())
  );
  const handleBookAppointment = (doctor: Doctor) => {
    // In a real app, this would open a booking modal or redirect to Doctolib
    const confirm = window.confirm(
      `Voulez-vous être redirigé vers la page de réservation du ${doctor.name} ?`
    );
    if (confirm) {
      window.open('https://www.doctolib.fr', '_blank');
    }
  };
  const center: [number, number] = [48.8566, 2.3522]; // Paris center
  return (
    <div className="h-[calc(100vh-64px)] flex flex-col bg-slate-50">
      {/* Header Bar */}
      <div className="bg-white border-b border-slate-200 px-4 py-3 shadow-sm z-10">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row gap-4 items-center justify-between">
          <div className="flex items-center w-full sm:w-auto gap-2">
            <button
              onClick={onBack}
              className="p-2 -ml-2 text-slate-500 hover:text-slate-900 rounded-full hover:bg-slate-100 transition-colors">

              <ArrowLeft className="w-5 h-5" />
            </button>
            <h1 className="text-lg font-bold text-slate-900">
              Trouver un spécialiste
            </h1>
          </div>

          {/* Search Controls */}
          <div className="flex-1 w-full sm:max-w-2xl flex gap-2">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Spécialité, médecin, établissement..."
                className="w-full pl-9 pr-4 py-2 bg-slate-100 border-none rounded-lg text-sm focus:ring-2 focus:ring-blue-500" />

            </div>
            <div className="relative hidden sm:block w-1/3">
              <NavIcon className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
              <input
                type="text"
                value={userLocation}
                onChange={(e) => setUserLocation(e.target.value)}
                placeholder="Où ?"
                className="w-full pl-9 pr-4 py-2 bg-slate-100 border-none rounded-lg text-sm focus:ring-2 focus:ring-blue-500" />

            </div>
            <Button variant="outline" className="px-3">
              <Filter className="w-4 h-4" />
            </Button>
          </div>
        </div>
      </div>

      {/* Mobile View Toggles */}
      <div className="sm:hidden bg-white border-b border-slate-200 px-4 py-2 flex gap-2">
        <button
          onClick={() => setViewMode('list')}
          className={`flex-1 flex items-center justify-center gap-2 py-2 text-sm font-medium rounded-md transition-colors ${viewMode === 'list' ? 'bg-blue-50 text-blue-700' : 'text-slate-600 hover:bg-slate-50'}`}>

          <List className="w-4 h-4" />
          Liste
        </button>
        <button
          onClick={() => setViewMode('map')}
          className={`flex-1 flex items-center justify-center gap-2 py-2 text-sm font-medium rounded-md transition-colors ${viewMode === 'map' ? 'bg-blue-50 text-blue-700' : 'text-slate-600 hover:bg-slate-50'}`}>

          <MapIcon className="w-4 h-4" />
          Carte
        </button>
      </div>

      {/* Main Content - Split View */}
      <div className="flex-1 flex overflow-hidden relative">
        {/* List View */}
        <div
          className={`
          w-full lg:w-[450px] xl:w-[500px] bg-slate-50 flex-col border-r border-slate-200 overflow-y-auto
          ${viewMode === 'map' ? 'hidden lg:flex' : 'flex'}
        `}>

          <div className="p-4 space-y-4">
            <div className="flex items-center justify-between">
              <p className="text-sm text-slate-500">
                <strong>{filteredDoctors.length} résultats</strong> à proximité
                de {userLocation}
              </p>
              <span className="text-xs text-slate-400">
                Trié par pertinence
              </span>
            </div>

            <div className="space-y-4 pb-20 lg:pb-4">
              {filteredDoctors.map((doctor) =>
              <DoctorCard
                key={doctor.id}
                doctor={doctor}
                isSelected={selectedDoctorId === doctor.id}
                onClick={() => {
                  setSelectedDoctorId(doctor.id);
                  if (window.innerWidth < 1024) setViewMode('map');
                }}
                onBook={() => handleBookAppointment(doctor)} />

              )}
            </div>
          </div>
        </div>

        {/* Map View */}
        <div
          className={`
          flex-1 bg-slate-100 relative
          ${viewMode === 'list' ? 'hidden lg:block' : 'block'}
        `}>

          <SpecialistMap
            doctors={filteredDoctors}
            selectedDoctorId={selectedDoctorId}
            onMarkerClick={(id) => {
              setSelectedDoctorId(id);
              if (window.innerWidth < 1024) {


                // On mobile, maybe show a bottom sheet or just switch to list?
                // For now, we'll keep map open but maybe show a floating card
              }}} center={center} />


          {/* Mobile Floating Card when marker selected */}
          {selectedDoctorId && viewMode === 'map' &&
          <div className="absolute bottom-4 left-4 right-4 z-[1000] lg:hidden">
              <div className="relative">
                <button
                onClick={() => setSelectedDoctorId(undefined)}
                className="absolute -top-3 -right-3 bg-white rounded-full p-1 shadow-md z-10">

                  <span className="sr-only">Fermer</span>
                  <svg
                  className="w-4 h-4 text-slate-500"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor">

                    <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12" />

                  </svg>
                </button>
                <DoctorCard
                doctor={
                filteredDoctors.find((d) => d.id === selectedDoctorId)!
                }
                onBook={() =>
                handleBookAppointment(
                  filteredDoctors.find((d) => d.id === selectedDoctorId)!
                )
                } />

              </div>
            </div>
          }
        </div>
      </div>
    </div>);

}