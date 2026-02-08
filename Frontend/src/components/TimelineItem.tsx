import React from 'react';
import { Card } from './ui/Card';
import { Badge } from './ui/Badge';
import { Calendar, ArrowRight, Activity } from 'lucide-react';
interface TimelineItemProps {
  date: string;
  type: 'Initial' | 'Suivi';
  symptoms: string[];
  confidence: number;
  status?: 'Amélioration' | 'Stable' | 'Aggravation';
  isLast?: boolean;
  onClick?: () => void;
}
export function TimelineItem({
  date,
  type,
  symptoms,
  confidence,
  status,
  isLast = false,
  onClick
}: TimelineItemProps) {
  const getStatusColor = (status?: string) => {
    switch (status) {
      case 'Amélioration':
        return 'text-emerald-600 bg-emerald-50 border-emerald-100';
      case 'Aggravation':
        return 'text-red-600 bg-red-50 border-red-100';
      default:
        return 'text-amber-600 bg-amber-50 border-amber-100';
    }
  };
  return (
    <div className="relative pl-8 pb-8">
      {/* Vertical Line */}
      {!isLast &&
      <div className="absolute left-3.5 top-8 bottom-0 w-0.5 bg-slate-200" />
      }

      {/* Dot */}
      <div
        className={`absolute left-0 top-1.5 w-7 h-7 rounded-full border-4 border-white shadow-sm flex items-center justify-center ${type === 'Initial' ? 'bg-blue-600' : 'bg-slate-400'}`}>

        {type === 'Initial' ?
        <Activity className="w-3 h-3 text-white" /> :

        <div className="w-2 h-2 bg-white rounded-full" />
        }
      </div>

      {/* Content */}
      <div
        className="group cursor-pointer transition-transform hover:-translate-y-1"
        onClick={onClick}>

        <Card noPadding className="p-4 hover:border-blue-300 transition-colors">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <Badge variant={type === 'Initial' ? 'default' : 'neutral'}>
                  {type}
                </Badge>
                <span className="text-xs text-slate-500 flex items-center">
                  <Calendar className="w-3 h-3 mr-1" />
                  {date}
                </span>
              </div>

              <div className="flex flex-wrap gap-1 mb-2">
                {symptoms.slice(0, 3).map((s, i) =>
                <span
                  key={i}
                  className="text-xs bg-slate-100 text-slate-600 px-2 py-0.5 rounded">

                    {s}
                  </span>
                )}
                {symptoms.length > 3 &&
                <span className="text-xs text-slate-400 px-1">
                    +{symptoms.length - 3}
                  </span>
                }
              </div>
            </div>

            <div className="flex items-center gap-4">
              {status &&
              <span
                className={`text-xs font-medium px-2 py-1 rounded-full border ${getStatusColor(status)}`}>

                  {status}
                </span>
              }
              <div className="text-right min-w-[60px]">
                <div className="text-xs text-slate-400">Confiance</div>
                <div className="font-bold text-slate-700">{confidence}%</div>
              </div>
              <ArrowRight className="w-4 h-4 text-slate-300 group-hover:text-blue-600" />
            </div>
          </div>
        </Card>
      </div>
    </div>);

}