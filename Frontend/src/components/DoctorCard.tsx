import React from 'react';
import {
  MapPin,
  Star,
  Calendar,
  Phone,
  Clock,
  ExternalLink } from
'lucide-react';
import { Card } from './ui/Card';
import { Button } from './ui/Button';
import { Badge } from './ui/Badge';
export interface Doctor {
  id: string;
  name: string;
  specialty: string;
  address: string;
  distance: string;
  rating: number;
  reviewCount: number;
  nextAvailability: string;
  imageUrl: string;
  isConventionne: boolean;
  coordinates: [number, number];
}
interface DoctorCardProps {
  doctor: Doctor;
  isSelected?: boolean;
  onClick?: () => void;
  onBook?: () => void;
}
export function DoctorCard({
  doctor,
  isSelected,
  onClick,
  onBook
}: DoctorCardProps) {
  return (
    <Card
      className={`transition-all duration-200 cursor-pointer hover:shadow-md ${isSelected ? 'ring-2 ring-blue-600 border-transparent' : 'border-slate-200'}`}
      noPadding
      onClick={onClick}>

      <div className="p-4 sm:p-5 flex gap-4">
        {/* Avatar */}
        <div className="flex-shrink-0">
          <img
            src={doctor.imageUrl}
            alt={doctor.name}
            className="w-16 h-16 sm:w-20 sm:h-20 rounded-lg object-cover bg-slate-100 border border-slate-100" />

        </div>

        {/* Content */}
        <div className="flex-1 min-w-0">
          <div className="flex justify-between items-start">
            <div>
              <h3 className="font-bold text-slate-900 text-lg truncate pr-2">
                {doctor.name}
              </h3>
              <p className="text-blue-600 font-medium text-sm mb-1">
                {doctor.specialty}
              </p>
            </div>
            {doctor.isConventionne &&
            <Badge variant="success" className="flex-shrink-0">
                Secteur 1
              </Badge>
            }
          </div>

          <div className="flex items-center gap-1 mb-2">
            <Star className="w-4 h-4 text-amber-400 fill-current" />
            <span className="font-bold text-slate-900 text-sm">
              {doctor.rating}
            </span>
            <span className="text-slate-500 text-xs">
              ({doctor.reviewCount} avis)
            </span>
          </div>

          <div className="flex items-start gap-1.5 text-sm text-slate-600 mb-3">
            <MapPin className="w-4 h-4 flex-shrink-0 mt-0.5 text-slate-400" />
            <span className="line-clamp-1">
              {doctor.address} • <strong>{doctor.distance}</strong>
            </span>
          </div>

          <div className="flex items-center gap-2 text-sm text-emerald-700 bg-emerald-50 px-3 py-1.5 rounded-md w-fit mb-4">
            <Calendar className="w-4 h-4" />
            <span className="font-medium">
              Prochain rdv : {doctor.nextAvailability}
            </span>
          </div>

          <div className="flex gap-2 mt-auto">
            <Button
              size="sm"
              className="flex-1 bg-[#1E40AF]"
              onClick={(e) => {
                e.stopPropagation();
                onBook?.();
              }}>

              Prendre RDV
            </Button>
            <Button
              size="sm"
              variant="outline"
              className="px-3"
              onClick={(e) => {
                e.stopPropagation();
                // Mock call action
                alert(`Appel au secrétariat de ${doctor.name}`);
              }}>

              <Phone className="w-4 h-4" />
            </Button>
          </div>
        </div>
      </div>
    </Card>);

}