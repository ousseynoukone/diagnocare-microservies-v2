import React, { useEffect, useMemo, useState } from 'react';
import {
  Plus,
  Clock,
  ChevronRight,
  Activity,
  AlertTriangle,
  Calendar,
  FileText } from
'lucide-react';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { StatCard } from '../components/StatCard';
import { RedFlagBanner } from '../components/RedFlagBanner';
import { getPredictionsByUser, getCheckIns } from '../api/diagnocare';
import type { PredictionDTO, CheckInResponse } from '../api/diagnocare';
import { tokenStorage } from '../api/storage';
interface DashboardPageProps {
  onNewEvaluation: () => void;
  onNavigate: (page: string) => void;
}
export function DashboardPage({
  onNewEvaluation,
  onNavigate
}: DashboardPageProps) {
  const [predictions, setPredictions] = useState<PredictionDTO[]>([]);
  const [checkIns, setCheckIns] = useState<CheckInResponse[]>([]);
  const user = tokenStorage.getUser<{ id: number; firstName?: string }>();

  useEffect(() => {
    if (!user?.id) {
      return;
    }
    getPredictionsByUser(user.id)
      .then(setPredictions)
      .catch(() => setPredictions([]));
    getCheckIns(user.id)
      .then(setCheckIns)
      .catch(() => setCheckIns([]));
  }, [user?.id]);

  const hasRedFlag = predictions.some((p) => p.isRedAlert);
  const recentEvaluations = useMemo(() => {
    return predictions.slice(0, 3).map((p) => ({
      id: p.id,
      date: p.createdAt ?? '—',
      result: p.isRedAlert ? 'Attention requise' : 'Évaluation terminée',
      confidence: p.bestScore ? Math.round(p.bestScore) : 0,
      urgent: Boolean(p.isRedAlert)
    }));
  }, [predictions]);

  return (
    <div className="min-h-screen bg-slate-50 p-4 sm:p-6 lg:p-8">
      <div className="max-w-5xl mx-auto space-y-8">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">
              Tableau de bord
            </h1>
            <p className="text-slate-500 mt-1">
              Bienvenue {user?.firstName ?? 'Utilisateur'}, voici votre aperçu santé du jour.
            </p>
          </div>
          <div className="text-sm text-slate-500 bg-white px-3 py-1 rounded-full border border-slate-200 shadow-sm">
            Dernière màj: à l'instant
          </div>
        </div>

        {/* Stats Row */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <StatCard
            title="Total évaluations"
            value={String(predictions.length)}
            icon={FileText}
            color="blue"
            trend={predictions.length > 0 ? `+${predictions.length}` : undefined}
            trendUp={predictions.length > 0}
            description="ce mois-ci" />

          <StatCard
            title="Alertes actives"
            value={String(predictions.filter((p) => p.isRedAlert).length)}
            icon={AlertTriangle}
            color="red"
            description="Action requise" />

          <StatCard
            title="Prochain suivi"
            value={checkIns.find((c) => c.status !== 'COMPLETED') ? '24h' : '—'}
            icon={Clock}
            color="amber"
            description="Suivi en attente" />

        </div>

        {/* Red Flag Alert */}
        {hasRedFlag && <RedFlagBanner />}

        {/* Quick Actions */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* New Evaluation CTA */}
          <button
            onClick={onNewEvaluation}
            className="group relative overflow-hidden bg-[#1E40AF] hover:bg-blue-900 transition-all duration-300 rounded-xl p-8 text-left shadow-lg shadow-blue-900/20">

            <div className="relative z-10 flex items-center justify-between">
              <div>
                <h2 className="text-2xl font-bold text-white mb-2">
                  Nouvelle évaluation
                </h2>
                <p className="text-blue-100 max-w-xs">
                  Lancez une analyse complète de vos symptômes assistée par IA.
                </p>
              </div>
              <div className="bg-white/10 p-3 rounded-full backdrop-blur-sm group-hover:bg-white/20 transition-colors">
                <Plus className="w-8 h-8 text-white" />
              </div>
            </div>
            {/* Decorative background pattern */}
            <div className="absolute top-0 right-0 -mt-4 -mr-4 w-32 h-32 bg-white/5 rounded-full blur-2xl" />
            <div className="absolute bottom-0 left-0 -mb-4 -ml-4 w-24 h-24 bg-blue-500/20 rounded-full blur-xl" />
          </button>

          {/* Schedule Follow-up CTA */}
          <button
            onClick={() => onNavigate('followup')}
            className="group relative overflow-hidden bg-white border border-slate-200 hover:border-blue-300 transition-all duration-300 rounded-xl p-8 text-left shadow-sm hover:shadow-md">

            <div className="relative z-10 flex items-center justify-between">
              <div>
                <h2 className="text-2xl font-bold text-slate-900 mb-2">
                  Planifier un suivi
                </h2>
                <p className="text-slate-500 max-w-xs">
                  Mettez à jour l'évolution de vos symptômes récents.
                </p>
              </div>
              <div className="bg-blue-50 p-3 rounded-full group-hover:bg-blue-100 transition-colors">
                <Calendar className="w-8 h-8 text-blue-600" />
              </div>
            </div>
          </button>
        </div>

        {/* Recent Evaluations */}
        <section>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-slate-900 flex items-center gap-2">
              <Clock className="w-5 h-5 text-slate-400" />
              Évaluations récentes
            </h2>
            <Button
              variant="ghost"
              size="sm"
              className="text-blue-700"
              onClick={() => onNavigate('history')}>

              Voir tout l'historique
            </Button>
          </div>

          <div className="space-y-4">
            {recentEvaluations.map((evalItem) =>
            <Card
              key={evalItem.id}
              className="hover:border-blue-200 transition-colors cursor-pointer group"
              noPadding
              onClick={() => onNavigate('results')}
            >
                <div className="p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div className="flex items-start gap-4">
                    <div
                    className={`p-2.5 rounded-lg ${evalItem.urgent ? 'bg-red-50' : 'bg-blue-50'}`}>

                      <Activity
                      className={`w-5 h-5 ${evalItem.urgent ? 'text-red-600' : 'text-blue-600'}`} />

                    </div>
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <span className="font-semibold text-slate-900">
                          {evalItem.result}
                        </span>
                        {evalItem.urgent &&
                      <Badge variant="danger">Attention requise</Badge>
                      }
                      </div>
                      <p className="text-sm text-slate-500 mb-1">
                        {evalItem.symptoms}
                      </p>
                      <p className="text-xs text-slate-400">{evalItem.date}</p>
                    </div>
                  </div>

                  <div className="flex items-center justify-between sm:justify-end gap-4 pl-14 sm:pl-0">
                    <div className="text-right">
                      <span className="text-xs font-medium text-slate-500 uppercase tracking-wider block">
                        Confiance
                      </span>
                      <span
                      className={`font-bold ${evalItem.confidence > 80 ? 'text-emerald-600' : 'text-amber-600'}`}>

                        {evalItem.confidence}%
                      </span>
                    </div>
                    <ChevronRight className="w-5 h-5 text-slate-300 group-hover:text-blue-600 transition-colors" />
                  </div>
                </div>
              </Card>
            )}
          </div>
        </section>
      </div>
    </div>);

}