import React from 'react';
import { Activity, Heart, Mail, Shield } from 'lucide-react';
export function Footer() {
  return (
    <footer className="bg-slate-900 text-slate-300 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-4 gap-8">
        <div className="col-span-1 md:col-span-2">
          <div className="flex items-center gap-2 mb-4">
            <div className="bg-blue-600 p-1.5 rounded-md">
              <Activity className="h-5 w-5 text-white" />
            </div>
            <span className="font-bold text-xl text-white tracking-tight">
              DiagnoCare
            </span>
          </div>
          <p className="text-slate-400 text-sm max-w-xs mb-6">
            Votre compagnon santé intelligent pour une prise en charge rapide et
            sereine. Technologie médicale avancée, accessible à tous.
          </p>
          <div className="flex gap-4">
            <a href="#" className="hover:text-white transition-colors">
              <Mail className="w-5 h-5" />
            </a>
            <a href="#" className="hover:text-white transition-colors">
              <Shield className="w-5 h-5" />
            </a>
            <a href="#" className="hover:text-white transition-colors">
              <Heart className="w-5 h-5" />
            </a>
          </div>
        </div>

        <div>
          <h3 className="text-white font-semibold mb-4">Produit</h3>
          <ul className="space-y-2 text-sm">
            <li>
              <a href="#" className="hover:text-blue-400 transition-colors">
                Fonctionnalités
              </a>
            </li>
            <li>
              <a href="#" className="hover:text-blue-400 transition-colors">
                Pour les médecins
              </a>
            </li>
            <li>
              <a href="#" className="hover:text-blue-400 transition-colors">
                Sécurité
              </a>
            </li>
            <li>
              <a href="#" className="hover:text-blue-400 transition-colors">
                Tarifs
              </a>
            </li>
          </ul>
        </div>

        <div>
          <h3 className="text-white font-semibold mb-4">Légal</h3>
          <ul className="space-y-2 text-sm">
            <li>
              <a href="#" className="hover:text-blue-400 transition-colors">
                Mentions légales
              </a>
            </li>
            <li>
              <a href="#" className="hover:text-blue-400 transition-colors">
                Confidentialité
              </a>
            </li>
            <li>
              <a href="#" className="hover:text-blue-400 transition-colors">
                CGU
              </a>
            </li>
            <li>
              <a href="#" className="hover:text-blue-400 transition-colors">
                Cookies
              </a>
            </li>
          </ul>
        </div>
      </div>

      <div className="max-w-5xl mx-auto mt-12 pt-8 border-t border-slate-800 text-center text-xs text-slate-500">
        <p>
          &copy; {new Date().getFullYear()} DiagnoCare SAS. Tous droits
          réservés.
        </p>
        <p className="mt-2">
          DiagnoCare n'est pas un dispositif médical de diagnostic. En cas
          d'urgence, appelez le 15.
        </p>
      </div>
    </footer>);

}