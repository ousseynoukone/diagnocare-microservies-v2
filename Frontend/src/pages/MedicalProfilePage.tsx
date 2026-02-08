import React, { useEffect, useState } from 'react';
import { Save, User, Activity, Heart, Info } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { getPatientProfile, upsertPatientProfile } from '../api/diagnocare';
import type { PatientMedicalProfile } from '../api/diagnocare';
import { tokenStorage } from '../api/storage';
export function MedicalProfilePage() {
  const [isSaving, setIsSaving] = useState(false);
  const user = tokenStorage.getUser<{ id: number }>();
  const [profile, setProfile] = useState<PatientMedicalProfile>({
    userId: user?.id ?? 0,
    familyAntecedents: []
  });

  useEffect(() => {
    if (!user?.id) {
      return;
    }
    getPatientProfile(user.id)
      .then((data) => setProfile({ ...data, userId: user.id }))
      .catch(() => setProfile((prev) => ({ ...prev, userId: user.id })));
  }, [user?.id]);
  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    try {
      await upsertPatientProfile(profile);
      alert('Profil mis à jour avec succès !');
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Erreur lors de la sauvegarde.');
    } finally {
      setIsSaving(false);
    }
  };
  return (
    <div className="min-h-screen bg-slate-50 p-4 sm:p-6 lg:p-8">
      <div className="max-w-3xl mx-auto space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-slate-900">
              Profil Médical
            </h1>
            <p className="text-slate-500 mt-1">
              Ces informations permettent d'affiner la précision de nos
              prédictions.
            </p>
          </div>
          <Button onClick={handleSave} disabled={isSaving} className="gap-2">
            <Save className="w-4 h-4" />
            {isSaving ? 'Enregistrement...' : 'Enregistrer'}
          </Button>
        </div>

        <div className="bg-blue-50 border border-blue-100 rounded-lg p-4 flex items-start gap-3">
          <Info className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5" />
          <p className="text-sm text-blue-800">
            Vos données sont stockées de manière sécurisée et ne sont utilisées
            que pour l'analyse de vos symptômes. Elles ne sont jamais partagées
            avec des tiers publicitaires.
          </p>
        </div>

        <form onSubmit={handleSave} className="space-y-6">
          {/* Basic Info */}
          <Card>
            <div className="flex items-center gap-2 mb-6 border-b border-slate-100 pb-4">
              <User className="w-5 h-5 text-slate-400" />
              <h2 className="text-lg font-semibold text-slate-900">
                Informations de base
              </h2>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Âge
                </label>
                <input
                  type="number"
                  className="w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  placeholder="Ex: 35"
                  value={profile.age ?? ''}
                  onChange={(e) => setProfile({ ...profile, age: Number(e.target.value) })} />

              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Sexe biologique
                </label>
                <select
                  className="w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  value={profile.gender ?? ''}
                  onChange={(e) => setProfile({ ...profile, gender: e.target.value })}>
                  <option value="">Non précisé</option>
                  <option value="MALE">Homme</option>
                  <option value="FEMALE">Femme</option>
                  <option value="OTHER">Autre</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Poids (kg)
                </label>
                <input
                  type="number"
                  className="w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  placeholder="Ex: 75"
                  value={profile.weight ?? ''}
                  onChange={(e) => setProfile({ ...profile, weight: Number(e.target.value) })} />

              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  IMC
                </label>
                <input
                  type="number"
                  className="w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  placeholder="Ex: 24"
                  value={profile.bmi ?? ''}
                  onChange={(e) => setProfile({ ...profile, bmi: Number(e.target.value) })} />

              </div>
            </div>
          </Card>

          {/* Health Metrics */}
          <Card>
            <div className="flex items-center gap-2 mb-6 border-b border-slate-100 pb-4">
              <Activity className="w-5 h-5 text-slate-400" />
              <h2 className="text-lg font-semibold text-slate-900">
                Indicateurs de santé
              </h2>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Tension artérielle moyenne
                </label>
                <input
                  type="number"
                  className="w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  placeholder="Ex: 120"
                  value={profile.meanBloodPressure ?? ''}
                  onChange={(e) => setProfile({ ...profile, meanBloodPressure: Number(e.target.value) })} />

              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Cholestérol total (g/L)
                </label>
                <input
                  type="number"
                  step="0.01"
                  className="w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  placeholder="Ex: 190"
                  value={profile.meanCholesterol ?? ''}
                  onChange={(e) => setProfile({ ...profile, meanCholesterol: Number(e.target.value) })} />

              </div>
              <div className="col-span-1 md:col-span-2">
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Antécédents familiaux majeurs
                </label>
                <textarea
                  rows={3}
                  className="w-full rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  placeholder="Diabète, hypertension, maladies cardiaques..."
                  value={(profile.familyAntecedents ?? []).join(', ')}
                  onChange={(e) =>
                    setProfile({
                      ...profile,
                      familyAntecedents: e.target.value
                        .split(',')
                        .map((item) => item.trim())
                        .filter(Boolean)
                    })
                  } />

              </div>
            </div>
          </Card>

          {/* Habits */}
          <Card>
            <div className="flex items-center gap-2 mb-6 border-b border-slate-100 pb-4">
              <Heart className="w-5 h-5 text-slate-400" />
              <h2 className="text-lg font-semibold text-slate-900">
                Habitudes de vie
              </h2>
            </div>

            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-slate-700">
                  Consommation de tabac
                </span>
                <div className="flex gap-4">
                  <label className="inline-flex items-center">
                    <input
                      type="radio"
                      name="tobacco"
                      className="form-radio text-blue-600"
                      checked={profile.isSmoking === true}
                      onChange={() => setProfile({ ...profile, isSmoking: true })} />

                    <span className="ml-2 text-sm text-slate-600">Oui</span>
                  </label>
                  <label className="inline-flex items-center">
                    <input
                      type="radio"
                      name="tobacco"
                      className="form-radio text-blue-600"
                      checked={profile.isSmoking === false}
                      onChange={() => setProfile({ ...profile, isSmoking: false })} />

                    <span className="ml-2 text-sm text-slate-600">Non</span>
                  </label>
                </div>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-slate-700">
                  Consommation d'alcool
                </span>
                <div className="flex gap-4">
                  <label className="inline-flex items-center">
                    <input
                      type="radio"
                      name="alcohol"
                      className="form-radio text-blue-600"
                      checked={profile.alcohol === true}
                      onChange={() => setProfile({ ...profile, alcohol: true })} />

                    <span className="ml-2 text-sm text-slate-600">
                      Régulière
                    </span>
                  </label>
                  <label className="inline-flex items-center">
                    <input
                      type="radio"
                      name="alcohol"
                      className="form-radio text-blue-600"
                      checked={profile.alcohol === false}
                      onChange={() => setProfile({ ...profile, alcohol: false })} />

                    <span className="ml-2 text-sm text-slate-600">
                      Occasionnelle
                    </span>
                  </label>
                  <label className="inline-flex items-center">
                    <input
                      type="radio"
                      name="alcohol"
                      className="form-radio text-blue-600"
                      defaultChecked />

                    <span className="ml-2 text-sm text-slate-600">Jamais</span>
                  </label>
                </div>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-slate-700">
                  Activité physique (sédentarité)
                </span>
                <select className="rounded-md border-slate-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 text-sm">
                  <option>Sédentaire (peu ou pas de sport)</option>
                  <option>Modérée (1-2 fois par semaine)</option>
                  <option>Active (3+ fois par semaine)</option>
                </select>
              </div>
            </div>
          </Card>
        </form>
      </div>
    </div>);

}