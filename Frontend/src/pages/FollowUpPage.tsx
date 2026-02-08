import React, { useEffect, useState } from 'react';
import {
  Calendar,
  ChevronRight,
  Clock,
  CheckCircle2,
  TrendingUp,
  TrendingDown,
  Minus,
  AlertCircle } from
'lucide-react';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { getCheckIns } from '../api/diagnocare';
import type { CheckInResponse } from '../api/diagnocare';
import { tokenStorage } from '../api/storage';
import { checkInState } from '../state/checkin';
interface FollowUpPageProps {
  onNavigate: (page: string) => void;
}
export function FollowUpPage({ onNavigate }: FollowUpPageProps) {
  const [activeTab, setActiveTab] = useState<'pending' | 'completed'>('pending');
  const user = tokenStorage.getUser<{ id: number }>();
  const [followUps, setFollowUps] = useState<CheckInResponse[]>([]);

  useEffect(() => {
    if (!user?.id) {
      return;
    }
    getCheckIns(user.id)
      .then(setFollowUps)
      .catch(() => setFollowUps([]));
  }, [user?.id]);

  const filteredFollowUps = followUps.filter((f) =>
    activeTab === 'pending' ? f.status !== 'COMPLETED' : f.status === 'COMPLETED'
  );
  const getEvolutionIcon = (evolution?: string) => {
    switch (evolution) {
      case 'better':
        return <TrendingUp className="w-4 h-4 text-emerald-500" />;
      case 'worse':
        return <TrendingDown className="w-4 h-4 text-red-500" />;
      default:
        return <Minus className="w-4 h-4 text-amber-500" />;
    }
  };
  const getEvolutionText = (evolution?: string) => {
    switch (evolution) {
      case 'better':
        return (
          <span className="text-emerald-600 font-medium">Amélioration</span>);

      case 'worse':
        return <span className="text-red-600 font-medium">Aggravation</span>;
      default:
        return <span className="text-amber-600 font-medium">Stable</span>;
    }
  };
  return (
    <div className="min-h-screen bg-slate-50 p-4 sm:p-6 lg:p-8">
      <div className="max-w-4xl mx-auto space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">
              Suivi de santé
            </h1>
            <p className="text-slate-500 mt-1">
              Gérez vos check-ins réguliers pour surveiller l'évolution de vos
              symptômes.
            </p>
          </div>
          <Button onClick={() => onNavigate('evaluation')}>
            <Clock className="w-4 h-4 mr-2" />
            Nouveau suivi
          </Button>
        </div>

        {/* Tabs */}
        <div className="flex border-b border-slate-200">
          <button
            onClick={() => setActiveTab('pending')}
            className={`pb-4 px-6 text-sm font-medium transition-colors relative ${activeTab === 'pending' ? 'text-blue-600 border-b-2 border-blue-600' : 'text-slate-500 hover:text-slate-700'}`}>

            En attente
            <span className="ml-2 bg-blue-100 text-blue-600 py-0.5 px-2 rounded-full text-xs">
              {followUps.filter((f) => f.status === 'pending').length}
            </span>
          </button>
          <button
            onClick={() => setActiveTab('completed')}
            className={`pb-4 px-6 text-sm font-medium transition-colors relative ${activeTab === 'completed' ? 'text-blue-600 border-b-2 border-blue-600' : 'text-slate-500 hover:text-slate-700'}`}>

            Terminés
          </button>
        </div>

        {/* List */}
        <div className="space-y-4">
          {filteredFollowUps.length === 0 ?
          <div className="text-center py-12 bg-white rounded-lg border border-slate-200 border-dashed">
              <CheckCircle2 className="w-12 h-12 text-slate-300 mx-auto mb-3" />
              <h3 className="text-lg font-medium text-slate-900">
                Aucun suivi {activeTab === 'pending' ? 'en attente' : 'terminé'}
              </h3>
              <p className="text-slate-500">Tout est à jour !</p>
            </div> :

          filteredFollowUps.map((item) =>
          <Card
            key={item.id}
            className="hover:border-blue-300 transition-all cursor-pointer group"
            noPadding>

                <div className="p-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                  <div className="flex items-start gap-4">
                    <div
                  className={`p-3 rounded-xl ${item.status !== 'COMPLETED' ? 'bg-blue-50 text-blue-600' : 'bg-slate-50 text-slate-500'}`}>

                      <Calendar className="w-6 h-6" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <h3 className="font-bold text-slate-900 text-lg">
                          Suivi #{item.previousPredictionId}
                        </h3>
                        {item.status !== 'COMPLETED' &&
                    <Badge variant="warning">À faire</Badge>
                    }
                      </div>
                      <div className="flex items-center gap-4 text-sm text-slate-500">
                        <span className="flex items-center gap-1">
                          <Clock className="w-4 h-4" />
                          {item.completedAt ?? item.firstReminderAt ?? '—'}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div className="w-full sm:w-auto flex items-center justify-between sm:justify-end gap-6 pl-16 sm:pl-0">
                    {item.status === 'COMPLETED' ?
                <div className="text-right">
                        <div className="flex items-center justify-end gap-1 mb-1">
                          {getEvolutionIcon(item.outcome === 'IMPROVING' ? 'better' : item.outcome === 'WORSENING' ? 'worse' : 'stable')}
                          {getEvolutionText(item.outcome === 'IMPROVING' ? 'better' : item.outcome === 'WORSENING' ? 'worse' : 'stable')}
                        </div>
                        <span className="text-xs text-slate-400">
                          Score {item.bestScoreDelta ?? '—'}
                        </span>
                      </div> :

                <Button
                  size="sm"
                  onClick={() => {
                    checkInState.setPreviousPredictionId(item.previousPredictionId);
                    onNavigate('evaluation');
                  }}>
                  Commencer
                </Button>
                }
                    <ChevronRight className="w-5 h-5 text-slate-300 group-hover:text-blue-600 transition-colors" />
                  </div>
                </div>
              </Card>
          )
          }
        </div>

        {/* Info Box */}
        <div className="bg-blue-50 border border-blue-100 rounded-lg p-4 flex items-start gap-3">
          <AlertCircle className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5" />
          <div>
            <h4 className="font-medium text-blue-900">
              Pourquoi le suivi est important ?
            </h4>
            <p className="text-sm text-blue-800 mt-1">
              Les symptômes évoluent rapidement. Un suivi régulier (toutes les
              24h ou 48h) permet à notre IA de détecter précocement toute
              aggravation et d'ajuster ses recommandations.
            </p>
          </div>
        </div>
      </div>
    </div>);

}