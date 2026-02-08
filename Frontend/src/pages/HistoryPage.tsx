import React, { useEffect, useMemo, useState } from 'react';
import {
  Filter,
  Calendar,
  AlertTriangle,
  ChevronLeft,
  ChevronRight,
  Search } from
'lucide-react';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { getPredictionsByUser, getPathologyResults } from '../api/diagnocare';
import type { PredictionDTO } from '../api/diagnocare';
import { tokenStorage } from '../api/storage';
interface HistoryPageProps {
  onNavigate: (page: string) => void;
}
export function HistoryPage({ onNavigate }: HistoryPageProps) {
  const [filter, setFilter] = useState('all');
  const user = tokenStorage.getUser<{ id: number }>();
  const [predictions, setPredictions] = useState<PredictionDTO[]>([]);
  const [pathologyMap, setPathologyMap] = useState<Record<number, { pathology?: string; specialist?: string }>>({});

  useEffect(() => {
    if (!user?.id) {
      return;
    }
    getPredictionsByUser(user.id)
      .then(setPredictions)
      .catch(() => setPredictions([]));
  }, [user?.id]);

  useEffect(() => {
    const run = async () => {
      const entries: Record<number, { pathology?: string; specialist?: string }> = {};
      await Promise.all(
        predictions.map(async (prediction) => {
          try {
            const results = await getPathologyResults(prediction.id);
            if (results[0]) {
              entries[prediction.id] = {
                pathology: results[0].pathologyName,
                specialist: results[0].doctorSpecialistLabel
              };
            }
          } catch {
            entries[prediction.id] = {};
          }
        })
      );
      setPathologyMap(entries);
    };
    if (predictions.length > 0) {
      run();
    }
  }, [predictions]);

  const historyItems = useMemo(() => {
    return predictions.map((prediction) => ({
      id: prediction.id,
      date: prediction.createdAt ?? '—',
      pathology: pathologyMap[prediction.id]?.pathology ?? '—',
      specialist: pathologyMap[prediction.id]?.specialist ?? '—',
      confidence: prediction.bestScore ? Math.round(prediction.bestScore) : 0,
      redFlag: Boolean(prediction.isRedAlert),
      type: prediction.previousPredictionId ? 'Suivi' : 'Initial'
    }));
  }, [predictions, pathologyMap]);

  return (
    <div className="min-h-screen bg-slate-50 p-4 sm:p-6 lg:p-8">
      <div className="max-w-5xl mx-auto space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">
              Historique des prédictions
            </h1>
            <p className="text-slate-500 mt-1">
              Retrouvez toutes vos anciennes analyses.
            </p>
          </div>
        </div>

        {/* Filters */}
        <Card noPadding className="p-4">
          <div className="flex flex-col sm:flex-row gap-4 items-center justify-between">
            <div className="relative w-full sm:w-64">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400 w-4 h-4" />
              <input
                type="text"
                placeholder="Rechercher..."
                className="w-full pl-9 pr-4 py-2 text-sm border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />

            </div>

            <div className="flex gap-2 w-full sm:w-auto overflow-x-auto pb-2 sm:pb-0">
              <button
                onClick={() => setFilter('all')}
                className={`px-3 py-1.5 text-sm font-medium rounded-lg whitespace-nowrap ${filter === 'all' ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'}`}>

                Tout voir
              </button>
              <button
                onClick={() => setFilter('redflag')}
                className={`px-3 py-1.5 text-sm font-medium rounded-lg whitespace-nowrap flex items-center gap-1 ${filter === 'redflag' ? 'bg-red-100 text-red-700' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'}`}>

                <AlertTriangle className="w-3 h-3" />
                Red Flags
              </button>
              <button
                onClick={() => setFilter('month')}
                className={`px-3 py-1.5 text-sm font-medium rounded-lg whitespace-nowrap flex items-center gap-1 ${filter === 'month' ? 'bg-blue-100 text-blue-700' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'}`}>

                <Calendar className="w-3 h-3" />
                Ce mois
              </button>
            </div>
          </div>
        </Card>

        {/* List */}
        <div className="space-y-4">
          {historyItems.map((item) =>
          <Card
            key={item.id}
            className="hover:border-blue-300 transition-colors cursor-pointer"
            onClick={() => onNavigate('results')}>

              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div className="flex items-start gap-4">
                  <div
                  className={`p-3 rounded-lg ${item.redFlag ? 'bg-red-50' : 'bg-blue-50'}`}>

                    {item.redFlag ?
                  <AlertTriangle className="w-6 h-6 text-red-600" /> :

                  <Calendar className="w-6 h-6 text-blue-600" />
                  }
                  </div>
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <h3 className="font-bold text-slate-900">
                        {item.pathology}
                      </h3>
                      {item.redFlag && <Badge variant="danger">Alerte</Badge>}
                    </div>
                    <p className="text-sm text-slate-500">
                      Recommandation : {item.specialist}
                    </p>
                  </div>
                </div>

                <div className="flex items-center justify-between sm:justify-end gap-6 pl-16 sm:pl-0">
                  <div className="text-right">
                    <span className="text-xs text-slate-400 block">Date</span>
                    <span className="text-sm font-medium text-slate-700">
                      {item.date}
                    </span>
                  </div>
                  <div className="text-right">
                    <span className="text-xs text-slate-400 block">
                      Confiance
                    </span>
                    <span className="text-sm font-bold text-slate-900">
                      {item.confidence}%
                    </span>
                  </div>
                  <Button variant="ghost" size="sm">
                    Détails
                  </Button>
                </div>
              </div>
            </Card>
          )}
        </div>

        {/* Pagination */}
        <div className="flex items-center justify-between pt-4 border-t border-slate-200">
          <Button variant="outline" size="sm" disabled>
            <ChevronLeft className="w-4 h-4 mr-2" />
            Précédent
          </Button>
          <span className="text-sm text-slate-500">Page 1 sur 3</span>
          <Button variant="outline" size="sm">
            Suivant
            <ChevronRight className="w-4 h-4 ml-2" />
          </Button>
        </div>
      </div>
    </div>);

}