import React from 'react';
import { Card } from './ui/Card';
import { BoxIcon } from 'lucide-react';
interface StatCardProps {
  title: string;
  value: string | number;
  icon: BoxIcon;
  trend?: string;
  trendUp?: boolean;
  description?: string;
  color?: 'blue' | 'emerald' | 'amber' | 'red';
}
export function StatCard({
  title,
  value,
  icon: Icon,
  trend,
  trendUp,
  description,
  color = 'blue'
}: StatCardProps) {
  const colorStyles = {
    blue: 'bg-blue-50 text-blue-600',
    emerald: 'bg-emerald-50 text-emerald-600',
    amber: 'bg-amber-50 text-amber-600',
    red: 'bg-red-50 text-red-600'
  };
  return (
    <Card
      noPadding
      className="p-5 flex items-start justify-between hover:shadow-md transition-shadow">

      <div>
        <p className="text-sm font-medium text-slate-500">{title}</p>
        <h3 className="text-2xl font-bold text-slate-900 mt-1">{value}</h3>
        {(trend || description) &&
        <div className="mt-1 flex items-center text-xs">
            {trend &&
          <span
            className={`font-medium ${trendUp ? 'text-emerald-600' : 'text-red-600'} mr-2`}>

                {trend}
              </span>
          }
            {description &&
          <span className="text-slate-400">{description}</span>
          }
          </div>
        }
      </div>
      <div className={`p-3 rounded-lg ${colorStyles[color]}`}>
        <Icon className="w-6 h-6" />
      </div>
    </Card>);

}