import React, { useState } from 'react';
import {
  Bell,
  Globe,
  Download,
  LogOut,
  Shield,
  Trash2,
  Smartphone } from
'lucide-react';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { updateUser, deleteUser } from '../api/user';
import { tokenStorage } from '../api/storage';
interface SettingsPageProps {
  onLogout: () => void;
}
export function SettingsPage({ onLogout }: SettingsPageProps) {
  const user = tokenStorage.getUser<{ id: number; lang?: string }>();
  const [language, setLanguage] = useState(user?.lang ?? 'fr');
  return (
    <div className="min-h-screen bg-slate-50 p-4 sm:p-6 lg:p-8">
      <div className="max-w-3xl mx-auto space-y-8">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Paramètres</h1>
          <p className="text-slate-500 mt-1">
            Gérez vos préférences et la sécurité de votre compte.
          </p>
        </div>

        {/* Preferences */}
        <section>
          <h2 className="text-lg font-semibold text-slate-900 mb-4">
            Préférences
          </h2>
          <Card className="divide-y divide-slate-100" noPadding>
            <div className="p-4 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <Globe className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="font-medium text-slate-900">Langue</p>
                  <p className="text-sm text-slate-500">
                    Langue de l'interface
                  </p>
                </div>
              </div>
              <select
                className="rounded-md border-slate-300 text-sm focus:ring-blue-500 focus:border-blue-500"
                value={language}
                onChange={async (e) => {
                  const value = e.target.value;
                  setLanguage(value);
                  if (user?.id) {
                    await updateUser(user.id, { lang: value });
                  }
                }}>
                <option value="fr">Français</option>
                <option value="en">English</option>
              </select>
            </div>

            <div className="p-4 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <Bell className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="font-medium text-slate-900">Notifications</p>
                  <p className="text-sm text-slate-500">
                    Rappels de suivi et alertes
                  </p>
                </div>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  className="sr-only peer"
                  defaultChecked />

                <div className="w-11 h-6 bg-slate-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
              </label>
            </div>
          </Card>
        </section>

        {/* Data & Security */}
        <section>
          <h2 className="text-lg font-semibold text-slate-900 mb-4">
            Données & Sécurité
          </h2>
          <Card className="divide-y divide-slate-100" noPadding>
            <div className="p-4 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <Download className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="font-medium text-slate-900">
                    Exporter mes données
                  </p>
                  <p className="text-sm text-slate-500">
                    Télécharger une copie de vos données (JSON/PDF)
                  </p>
                </div>
              </div>
              <Button variant="outline" size="sm">
                Exporter
              </Button>
            </div>

            <div className="p-4 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <Shield className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="font-medium text-slate-900">Mot de passe</p>
                  <p className="text-sm text-slate-500">
                    Dernière modification il y a 3 mois
                  </p>
                </div>
              </div>
              <Button variant="outline" size="sm">
                Modifier
              </Button>
            </div>

            <div className="p-4">
              <div className="flex items-center gap-3 mb-4">
                <Smartphone className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="font-medium text-slate-900">Sessions actives</p>
                </div>
              </div>
              <div className="space-y-3">
                <div className="flex items-center justify-between text-sm">
                  <span className="text-slate-600">
                    Chrome sur MacOS (Actuel)
                  </span>
                  <span className="text-emerald-600 font-medium">En ligne</span>
                </div>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-slate-600">Safari sur iPhone 13</span>
                  <span className="text-slate-400">Il y a 2h</span>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  className="text-red-600 hover:text-red-700 hover:bg-red-50 w-full mt-2 justify-start px-0">

                  Déconnecter toutes les autres sessions
                </Button>
              </div>
            </div>
          </Card>
        </section>

        {/* Danger Zone */}
        <section>
          <h2 className="text-lg font-semibold text-red-600 mb-4">
            Zone de danger
          </h2>
          <Card className="border-red-100 bg-red-50/30">
            <div className="flex items-center justify-between">
              <div>
                <p className="font-medium text-slate-900">
                  Supprimer mon compte
                </p>
                <p className="text-sm text-slate-500">
                  Cette action est irréversible. Toutes vos données seront
                  effacées.
                </p>
              </div>
              <Button
                variant="outline"
                className="border-red-200 text-red-600 hover:bg-red-50 hover:text-red-700 hover:border-red-300"
                onClick={async () => {
                  if (!user?.id) {
                    return;
                  }
                  const confirmed = window.confirm(
                    'Confirmer la suppression de votre compte ? Cette action est irréversible.'
                  );
                  if (!confirmed) {
                    return;
                  }
                  await deleteUser(user.id);
                  tokenStorage.clear();
                  onLogout();
                }}>

                <Trash2 className="w-4 h-4 mr-2" />
                Supprimer
              </Button>
            </div>
          </Card>
        </section>

        <div className="pt-6 border-t border-slate-200">
          <Button
            onClick={onLogout}
            variant="secondary"
            fullWidth
            className="text-slate-600">

            <LogOut className="w-4 h-4 mr-2" />
            Se déconnecter
          </Button>
        </div>
      </div>
    </div>);

}