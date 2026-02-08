import React from 'react';
import { AlertTriangle } from 'lucide-react';
export function DisclaimerBanner() {
  return (
    <div className="bg-amber-50 border-t border-amber-100 p-4">
      <div className="max-w-5xl mx-auto flex items-start gap-3">
        <AlertTriangle className="w-5 h-5 text-amber-600 flex-shrink-0 mt-0.5" />
        <p className="text-sm text-amber-800">
          <strong>Avertissement important :</strong> DiagnoCare est un outil
          d'aide à la décision et ne remplace pas un avis médical professionnel.
          Ceci n'est pas un diagnostic médical. En cas d'urgence, contactez
          immédiatement le 15 ou rendez-vous aux urgences.
        </p>
      </div>
    </div>);

}