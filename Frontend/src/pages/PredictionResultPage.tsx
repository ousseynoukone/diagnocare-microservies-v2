import React, { useMemo } from 'react';
import {
  ArrowLeft,
  Download,
  Calendar,
  Activity,
  AlertTriangle,
  CheckCircle2,
  HelpCircle,
  MapPin } from
'lucide-react';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { DisclaimerBanner } from '../components/DisclaimerBanner';
import { RedFlagBanner } from '../components/RedFlagBanner';
import { predictionState } from '../state/prediction';
interface PredictionResultPageProps {
  onBack: () => void;
  onNavigate: (page: string) => void;
}
export function PredictionResultPage({
  onBack,
  onNavigate
}: PredictionResultPageProps) {
  const last = predictionState.getLast();
  const prediction = useMemo(() => {
    if (!last?.mlResults?.predictions?.length) {
      return null;
    }
    const [top, ...rest] = last.mlResults.predictions;
    return {
      date: last.prediction?.createdAt,
      isRedAlert: last.prediction?.isRedAlert ?? false,
      topPathology: {
        name: top.disease ?? 'Pathologie',
        confidence: Math.round((top.probability ?? 0) * 100),
        description: top.description ?? '',
        specialist: top.specialist ?? 'Spécialiste'
      },
      otherPathologies: rest.map((item) => ({
        name: item.disease ?? 'Pathologie',
        confidence: Math.round((item.probability ?? 0) * 100)
      })),
      recommendations: top.description ? [top.description] : []
    };
  }, [last]);
  const handleDownloadPDF = () => {
    if (!last?.prediction?.id) {
      alert('Aucune prédiction disponible.');
      return;
    }
    onNavigate('summary');
  };
  if (!prediction) {
    return (
      <div className="min-h-screen bg-slate-50 p-4 sm:p-6 lg:p-8">
        <div className="max-w-3xl mx-auto space-y-6">
          <Button variant="ghost" onClick={onBack}>
            <ArrowLeft className="w-5 h-5 mr-2" />
            Retour
          </Button>
          <Card>
            <p className="text-slate-600">
              Aucune prédiction à afficher. Lancez une nouvelle évaluation.
            </p>
            <Button className="mt-4" onClick={() => onNavigate('evaluation')}>
              Nouvelle évaluation
            </Button>
          </Card>
        </div>
      </div>
    );
  }
  return (
    <div className="min-h-screen bg-slate-50 p-4 sm:p-6 lg:p-8">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <button
            onClick={onBack}
            className="flex items-center text-slate-500 hover:text-slate-900 transition-colors">

            <ArrowLeft className="w-5 h-5 mr-2" />
            Retour
          </button>
          <div className="flex gap-3">
            <Button
              variant="outline"
              onClick={handleDownloadPDF}
              className="hidden sm:flex">

              <Download className="w-4 h-4 mr-2" />
              PDF
            </Button>
            <Button onClick={() => onNavigate('followup')}>
              <Calendar className="w-4 h-4 mr-2" />
              Planifier un suivi
            </Button>
          </div>
        </div>

        {/* Red Flag Banner */}
        {prediction.isRedAlert && <RedFlagBanner />}

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-6">
            {/* Top Prediction */}
            <Card className="border-t-4 border-t-blue-600 overflow-hidden">
              <div className="bg-blue-50/50 p-6 border-b border-blue-100">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <div className="p-2 bg-blue-100 rounded-lg">
                      <Activity className="w-6 h-6 text-blue-700" />
                    </div>
                    <div>
                      <h2 className="text-xl font-bold text-slate-900">
                        Résultat principal
                      </h2>
                      <p className="text-sm text-slate-500">
                        Basé sur vos symptômes déclarés
                      </p>
                    </div>
                  </div>
                  <Badge variant="default" className="text-sm px-3 py-1">
                    Confiance {prediction.topPathology.confidence}%
                  </Badge>
                </div>

                <h3 className="text-2xl font-bold text-blue-900 mb-2">
                  {prediction.topPathology.name}
                </h3>
                <p className="text-slate-700 leading-relaxed">
                  {prediction.topPathology.description}
                </p>

                {/* Confidence Bar */}
                <div className="mt-6">
                  <div className="flex justify-between text-xs font-medium text-slate-500 mb-1">
                    <span>Probabilité</span>
                    <span>{prediction.topPathology.confidence}%</span>
                  </div>
                  <div className="w-full bg-blue-200 rounded-full h-2.5">
                    <div
                      className="bg-blue-600 h-2.5 rounded-full transition-all duration-1000 ease-out"
                      style={{
                        width: `${prediction.topPathology.confidence}%`
                      }} />

                  </div>
                </div>
              </div>

              {/* Other possibilities */}
              <div className="p-6 bg-white">
                <h4 className="text-sm font-semibold text-slate-900 uppercase tracking-wider mb-4">
                  Autres possibilités analysées
                </h4>
                <div className="space-y-3">
                  {prediction.otherPathologies.map((pathology, idx) =>
                  <div
                    key={idx}
                    className="flex items-center justify-between p-3 rounded-lg bg-slate-50 border border-slate-100">

                      <span className="font-medium text-slate-700">
                        {pathology.name}
                      </span>
                      <span className="text-sm text-slate-500">
                        {pathology.confidence}%
                      </span>
                    </div>
                  )}
                </div>
              </div>
            </Card>

            <DisclaimerBanner />
          </div>

          {/* Sidebar Content */}
          <div className="space-y-6">
            {/* Specialist Recommendation */}
            <Card className="bg-slate-900 text-white border-slate-800">
              <div className="flex items-center gap-3 mb-4">
                <div className="p-2 bg-white/10 rounded-lg">
                  <div className="w-6 h-6 text-blue-300" />
                </div>
                <div>
                  <h3 className="font-bold text-lg">Spécialiste recommandé</h3>
                  <p className="text-blue-200 text-sm">
                    Pour confirmer ce résultat
                  </p>
                </div>
              </div>

              <div className="bg-white/5 rounded-lg p-4 mb-6 text-center border border-white/10">
                <span className="text-xl font-bold text-white block">
                  {prediction.topPathology.specialist}
                </span>
              </div>

              <Button
                fullWidth
                className="bg-white text-slate-900 hover:bg-blue-50 border-none"
                onClick={() => onNavigate('specialist-finder')}>

                <MapPin className="w-4 h-4 mr-2" />
                Trouver à proximité
              </Button>
              <p className="text-xs text-slate-400 text-center mt-3">
                Redirection vers Doctolib / Google Maps
              </p>
            </Card>

            {/* Recommendations */}
            <Card className="bg-emerald-50 border-emerald-100">
              <div className="flex items-center gap-2 mb-4">
                <CheckCircle2 className="w-5 h-5 text-emerald-600" />
                <h3 className="font-bold text-emerald-900">
                  Conseils immédiats
                </h3>
              </div>
              <ul className="space-y-2">
                {(prediction.recommendations ?? []).map((rec, idx) => (
                  <li
                    key={idx}
                    className="flex items-start gap-2 text-sm text-emerald-800">
                    <span className="mt-1.5 w-1.5 h-1.5 rounded-full bg-emerald-400 flex-shrink-0" />
                    {rec}
                  </li>
                ))}
                {(prediction.recommendations ?? []).length === 0 && (
                  <li className="text-sm text-emerald-800">
                    Suivez les recommandations de votre spécialiste.
                  </li>
                )}
              </ul>
            </Card>
          </div>
        </div>

        <div className="mt-8">
          <DisclaimerBanner />
        </div>
      </div>
    </div>);

}