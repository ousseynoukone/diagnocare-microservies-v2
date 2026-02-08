import React, { useEffect, useState } from 'react';
import { TimelineItem } from '../components/TimelineItem';
import { Button } from '../components/ui/Button';
import { Plus } from 'lucide-react';
import { getSummary } from '../api/diagnocare';
import type { ConsultationSummary } from '../api/diagnocare';
import { predictionState } from '../state/prediction';
interface TimelinePageProps {
  onNavigate: (page: string) => void;
}
export function TimelinePage({ onNavigate }: TimelinePageProps) {
  const [summary, setSummary] = useState<ConsultationSummary | null>(null);
  const last = predictionState.getLast();

  useEffect(() => {
    if (!last?.prediction?.id) {
      return;
    }
    getSummary(last.prediction.id)
      .then(setSummary)
      .catch(() => setSummary(null));
  }, [last?.prediction?.id]);

  const timelineEvents = summary?.timeline ?? [];

  return (
    <div className="min-h-screen bg-slate-50 p-4 sm:p-6 lg:p-8">
      <div className="max-w-3xl mx-auto">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">Chronologie</h1>
            <p className="text-slate-500 mt-1">
              L'historique complet de vos évaluations et suivis.
            </p>
          </div>
          <Button onClick={() => onNavigate('evaluation')}>
            <Plus className="w-4 h-4 mr-2" />
            Nouvelle entrée
          </Button>
        </div>

        <div className="bg-white rounded-xl border border-slate-200 shadow-sm p-6 sm:p-8">
          {timelineEvents.length === 0 ? (
            <p className="text-slate-500">Aucune chronologie disponible.</p>
          ) : (
            timelineEvents.map((event, index) => (
              <TimelineItem
                key={event.predictionId ?? index}
                date={event.date ?? '—'}
                type={event.type === 'Suivi' ? 'Suivi' : 'Initial'}
                symptoms={event.symptoms ?? []}
                confidence={event.score ? Math.round(event.score) : 0}
                status={
                  event.outcome === 'Amélioration'
                    ? 'Amélioration'
                    : event.outcome === 'Aggravation'
                      ? 'Aggravation'
                      : event.outcome
                        ? 'Stable'
                        : undefined
                }
                isLast={index === timelineEvents.length - 1}
                onClick={() => onNavigate('results')}
              />
            ))
          )}
        </div>
      </div>
    </div>);

}