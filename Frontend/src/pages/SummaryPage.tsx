import React, { useEffect, useState } from 'react';
import { Download, Printer, Share2, Calendar, User } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { getSummary, getSummaryPdfUrl } from '../api/diagnocare';
import type { ConsultationSummary } from '../api/diagnocare';
import { predictionState } from '../state/prediction';
export function SummaryPage() {
  const handlePrint = () => {
    window.print();
  };
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
  return (
    <div className="min-h-screen bg-slate-50 p-4 sm:p-6 lg:p-8 print:bg-white print:p-0">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Actions Bar (Hidden in print) */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 print:hidden">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">
              Résumé de consultation
            </h1>
            <p className="text-slate-500 mt-1">
              Document généré automatiquement pour votre médecin.
            </p>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" onClick={handlePrint}>
              <Printer className="w-4 h-4 mr-2" />
              Imprimer
            </Button>
            <Button
              onClick={() => {
                if (!last?.prediction?.id) {
                  return;
                }
                const url = getSummaryPdfUrl(last.prediction.id);
                window.open(url, '_blank');
              }}>
              <Download className="w-4 h-4 mr-2" />
              Télécharger PDF
            </Button>
          </div>
        </div>

        {/* Report Content */}
        <div className="bg-white shadow-lg rounded-xl overflow-hidden print:shadow-none print:rounded-none">
          {/* Header */}
          <div className="bg-slate-900 text-white p-8 print:bg-white print:text-black print:border-b-2 print:border-black">
            <div className="flex justify-between items-start">
              <div>
                <h2 className="text-2xl font-bold mb-1">
                  Rapport Médical DiagnoCare
                </h2>
                <p className="text-blue-200 print:text-slate-600 text-sm">
                  Généré le {summary?.generatedAt ?? '—'}
                </p>
              </div>
              <div className="text-right">
                <div className="inline-block px-3 py-1 bg-white/10 rounded border border-white/20 print:border-black print:bg-transparent">
                  <span className="text-xs font-mono">ID: #{last?.prediction?.id ?? '—'}</span>
                </div>
              </div>
            </div>
          </div>

          <div className="p-8 space-y-8">
            {/* Patient Info */}
            <section className="grid grid-cols-2 gap-8 pb-8 border-b border-slate-100">
              <div>
                <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3">
                  Patient
                </h3>
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-slate-100 rounded-full flex items-center justify-center print:hidden">
                    <User className="w-5 h-5 text-slate-500" />
                  </div>
                  <div>
                    <p className="font-bold text-slate-900">{summary?.patientName ?? '—'}</p>
                    <p className="text-sm text-slate-500">Profil patient</p>
                  </div>
                </div>
              </div>
              <div>
                <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3">
                  Contexte
                </h3>
                <p className="text-sm text-slate-600">
                  <span className="font-medium">Motif:</span>{' '}
                  {summary?.symptomsDescription || '—'}
                </p>
              </div>
            </section>

            {/* Symptoms */}
            <section>
              <h3 className="text-lg font-bold text-slate-900 mb-4 flex items-center gap-2">
                1. Symptômes déclarés
              </h3>
              <div className="flex flex-wrap gap-2 mb-4">
                {(summary?.symptoms ?? []).map((s, i) =>
                <span
                  key={i}
                  className="px-3 py-1 bg-slate-100 text-slate-700 rounded-full text-sm border border-slate-200 print:border-black">

                    {s}
                  </span>
                )}
              </div>
              <p className="text-slate-600 text-sm italic bg-slate-50 p-4 rounded-lg border-l-4 border-slate-300 print:border-black print:bg-white">
                "{summary?.symptomsDescription || '—'}"
              </p>
            </section>

            {/* Analysis */}
            <section>
              <h3 className="text-lg font-bold text-slate-900 mb-4 flex items-center gap-2">
                2. Analyse prédictive
              </h3>
              <div className="border border-slate-200 rounded-lg overflow-hidden">
                <table className="w-full text-sm text-left">
                  <thead className="bg-slate-50 text-slate-500 font-medium border-b border-slate-200">
                    <tr>
                      <th className="px-4 py-3">Pathologie potentielle</th>
                      <th className="px-4 py-3">Confiance</th>
                      <th className="px-4 py-3">Spécialiste</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-200">
                    {(summary?.pathologyDetails ?? []).map((item, idx) => (
                      <tr key={idx} className={idx === 0 ? 'bg-blue-50/50 print:bg-white' : undefined}>
                        <td className="px-4 py-3 font-bold text-slate-900">
                          {item.pathologyName ?? '—'}
                        </td>
                        <td className="px-4 py-3 text-blue-600 font-bold">
                          {item.diseaseScore ?? 0}%
                        </td>
                        <td className="px-4 py-3">{item.specialist ?? '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </section>

            {/* Questions */}
            <section>
              <h3 className="text-lg font-bold text-slate-900 mb-4 flex items-center gap-2">
                3. Questions suggérées pour la consultation
              </h3>
              <ul className="list-disc pl-5 space-y-2 text-slate-700">
                {(summary?.questionsForDoctor ?? []).map((question, idx) => (
                  <li key={idx}>{question}</li>
                ))}
              </ul>
            </section>

            {/* Footer Disclaimer */}
            <div className="mt-12 pt-6 border-t border-slate-200 text-center text-xs text-slate-400 print:text-black">
              <p>
                Ce document est généré par une IA à titre informatif. Il ne
                constitue pas un diagnostic médical officiel.
              </p>
              <p>DiagnoCare © 2023</p>
            </div>
          </div>
        </div>
      </div>
    </div>);

}