import React, { useEffect, useState } from 'react';
import {
  Search,
  X,
  ArrowRight,
  Activity,
  AlertCircle,
  CheckCircle2 } from
'lucide-react';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { getSymptoms, createPrediction, submitCheckIn, getPredictionsByUser } from '../api/diagnocare';
import type { SymptomDTO } from '../api/diagnocare';
import { tokenStorage } from '../api/storage';
import { predictionState } from '../state/prediction';
import { checkInState } from '../state/checkin';
interface SymptomEvaluationPageProps {
  onNavigate: (page: string) => void;
}
export function SymptomEvaluationPage({
  onNavigate
}: SymptomEvaluationPageProps) {
  const [step, setStep] = useState<'symptoms' | 'analysis'>('symptoms');
  const [selectedSymptoms, setSelectedSymptoms] = useState<string[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [symptoms, setSymptoms] = useState<SymptomDTO[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getSymptoms()
      .then(setSymptoms)
      .catch(() => setSymptoms([]));
  }, []);

  const filteredSymptoms = symptoms.filter((symptom) =>
    symptom.label.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const toggleSymptom = (symptom: string) => {
    if (selectedSymptoms.includes(symptom)) {
      setSelectedSymptoms(selectedSymptoms.filter((s) => s !== symptom));
    } else {
      setSelectedSymptoms([...selectedSymptoms, symptom]);
    }
  };
  const handleAnalyze = async () => {
    setStep('analysis');
    setIsLoading(true);
    setError(null);
    try {
      const user = tokenStorage.getUser<{ id: number }>();
      if (!user?.id) {
        throw new Error('Utilisateur non connecté');
      }
      const previousPredictionId = checkInState.getPreviousPredictionId();
      if (previousPredictionId) {
        await submitCheckIn({
          userId: user.id,
          previousPredictionId,
          symptomLabels: selectedSymptoms
        });
        const predictions = await getPredictionsByUser(user.id);
        const latest = predictions
          .filter((p) => p.createdAt)
          .sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? ''))[0];
        if (latest) {
          predictionState.setLast({
            prediction: latest,
            mlResults: { predictions: [] }
          });
        }
        checkInState.clear();
        onNavigate('summary');
      } else {
        const response = await createPrediction({
          userId: user.id,
          symptomLabels: selectedSymptoms,
          rawDescription: ''
        });
        predictionState.setLast(response);
        onNavigate('results');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erreur lors de la prédiction.');
      setStep('symptoms');
    } finally {
      setIsLoading(false);
    }
  };
  if (step === 'analysis') {
    return (
      <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-4">
        <div className="relative">
          <div className="w-16 h-16 border-4 border-blue-100 border-t-[#1E40AF] rounded-full animate-spin"></div>
          <Activity className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 text-[#1E40AF] w-6 h-6" />
        </div>
        <h2 className="mt-6 text-xl font-semibold text-slate-900">
          Analyse en cours...
        </h2>
        <p className="text-slate-500 mt-2">
          Notre IA compare vos symptômes avec notre base médicale.
        </p>
      </div>);

  }
  return (
    <div className="min-h-screen bg-slate-50 p-4 sm:p-6 lg:p-8 pb-32">
      <div className="max-w-3xl mx-auto">
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-slate-900">
            Évaluation des symptômes
          </h1>
          <p className="text-slate-500 mt-2">
            Sélectionnez ce que vous ressentez pour commencer l'analyse.
          </p>
        </div>

        <Card className="mb-6">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400 w-5 h-5" />
            <input
              type="text"
              placeholder="Rechercher un symptôme (ex: fièvre, douleur...)"
              className="w-full pl-10 pr-4 py-3 border-none focus:ring-0 text-slate-900 placeholder-slate-400"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)} />

          </div>
        </Card>

        {error &&
        <div className="mb-4 bg-red-50 text-red-700 border border-red-100 rounded-lg px-4 py-3 text-sm">
            {error}
          </div>
        }

        <div className="mb-8">
          <h3 className="text-sm font-medium text-slate-500 uppercase tracking-wider mb-4">
            Symptômes disponibles
          </h3>
          <div className="flex flex-wrap gap-2">
            {filteredSymptoms.map((symptom) =>
            <button
              key={symptom.id}
              onClick={() => toggleSymptom(symptom.label)}
              className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${selectedSymptoms.includes(symptom.label) ? 'bg-[#1E40AF] text-white shadow-md transform scale-105' : 'bg-white text-slate-600 border border-slate-200 hover:border-blue-300 hover:text-blue-600'}`}>

                {symptom.label}
              </button>
            )}
          </div>
        </div>

        {selectedSymptoms.length > 0 &&
        <div className="mb-6">
            <h3 className="text-sm font-medium text-slate-500 uppercase tracking-wider mb-3">
              Vos symptômes sélectionnés
            </h3>
            <div className="flex flex-wrap gap-2">
              {selectedSymptoms.map((s) =>
            <span
              key={s}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-blue-50 text-blue-700 text-sm font-medium border border-blue-100">

                  <CheckCircle2 className="w-3.5 h-3.5" />
                  {s}
                  <button
                onClick={() => toggleSymptom(s)}
                className="ml-0.5 text-blue-400 hover:text-blue-700">

                    <X className="w-3.5 h-3.5" />
                  </button>
                </span>
            )}
            </div>
          </div>
        }
      </div>

      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-slate-200 p-4 shadow-[0_-4px_20px_rgba(0,0,0,0.08)] z-40">
        <div className="max-w-3xl mx-auto flex items-center justify-between">
          <div className="text-sm text-slate-500">
            {selectedSymptoms.length === 0 ?
            <span className="flex items-center gap-2">
                <AlertCircle className="w-4 h-4 text-slate-400" />
                Sélectionnez au moins 1 symptôme
              </span> :

            <span className="flex items-center gap-2 text-blue-700 font-medium">
                <CheckCircle2 className="w-4 h-4" />
                {selectedSymptoms.length} symptôme
                {selectedSymptoms.length > 1 ? 's' : ''} sélectionné
                {selectedSymptoms.length > 1 ? 's' : ''}
              </span>
            }
          </div>
          <Button
            onClick={handleAnalyze}
            disabled={selectedSymptoms.length === 0 || isLoading}
            size="lg">

            Lancer l'analyse
            <ArrowRight className="w-5 h-5 ml-2" />
          </Button>
        </div>
      </div>
    </div>);

}