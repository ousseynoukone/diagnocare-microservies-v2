import React from 'react';
import { AlertOctagon, Phone } from 'lucide-react';
export function RedFlagBanner() {
  return (
    <div className="bg-red-50 border-l-4 border-red-600 p-4 mb-6 rounded-r-lg shadow-sm">
      <div className="flex items-start">
        <div className="flex-shrink-0">
          <AlertOctagon className="h-6 w-6 text-red-600" aria-hidden="true" />
        </div>
        <div className="ml-3">
          <h3 className="text-lg font-bold text-red-800">
            Attention : Situation nécessitant une prise en charge rapide
          </h3>
          <div className="mt-2 text-sm text-red-700">
            <p className="mb-2">
              D'après vos symptômes, une consultation médicale urgente est
              recommandée. Ne vous fiez pas uniquement à cette application.
            </p>
            <p className="font-bold">
              En cas de doute ou d'urgence vitale, contactez immédiatement le 15
              (SAMU) ou rendez-vous aux urgences.
            </p>
          </div>
          <div className="mt-4">
            <a
              href="tel:15"
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500">

              <Phone className="mr-2 h-4 w-4" />
              Appeler le 15
            </a>
          </div>
        </div>
      </div>
    </div>);

}