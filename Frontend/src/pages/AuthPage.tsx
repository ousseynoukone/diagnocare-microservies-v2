import React, { useEffect, useState } from 'react';
import {
  Activity,
  Mail,
  Lock,
  User,
  Phone,
  ArrowRight,
  AlertCircle } from
'lucide-react';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { DisclaimerBanner } from '../components/DisclaimerBanner';
import { login, register, fetchRoles } from '../api/auth';
import type { Role } from '../api/auth';
interface AuthPageProps {
  onLogin: () => void;
}
export function AuthPage({ onLogin }: AuthPageProps) {
  const [mode, setMode] = useState<'login' | 'register' | 'forgot'>('login');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [roles, setRoles] = useState<Role[]>([]);
  const [form, setForm] = useState({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phoneNumber: '',
    lang: 'fr'
  });

  useEffect(() => {
    fetchRoles()
      .then(setRoles)
      .catch(() => setRoles([]));
  }, []);

  const patientRoleId = roles.find((r) => r.name?.toUpperCase() === 'PATIENT')?.id ?? 1;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    try {
      if (mode === 'login') {
        await login({ email: form.email, password: form.password });
        onLogin();
      } else if (mode === 'register') {
        await register({
          email: form.email,
          password: form.password,
          firstName: form.firstName,
          lastName: form.lastName,
          phoneNumber: form.phoneNumber || undefined,
          lang: form.lang,
          roleId: patientRoleId
        });
        onLogin();
      } else {
        setError('La réinitialisation n’est pas encore disponible.');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erreur inattendue.');
    } finally {
      setIsLoading(false);
    }
  };
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="flex justify-center">
          <div className="bg-blue-600 p-3 rounded-xl shadow-lg shadow-blue-900/20">
            <Activity className="h-10 w-10 text-white" />
          </div>
        </div>
        <h2 className="mt-6 text-center text-3xl font-extrabold text-slate-900">
          {mode === 'login' && 'Connexion à votre espace'}
          {mode === 'register' && 'Créer un compte patient'}
          {mode === 'forgot' && 'Réinitialisation du mot de passe'}
        </h2>
        <p className="mt-2 text-center text-sm text-slate-600">
          {mode === 'login' &&
          'Accédez à vos suivis et résultats en toute sécurité.'}
          {mode === 'register' &&
          'Rejoignez DiagnoCare pour un suivi santé intelligent.'}
          {mode === 'forgot' && 'Nous vous enverrons un lien de récupération.'}
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <Card className="py-8 px-4 shadow sm:rounded-lg sm:px-10">
          <form className="space-y-6" onSubmit={handleSubmit}>
            {error &&
            <div className="rounded-md bg-red-50 p-4">
                <div className="flex">
                  <div className="flex-shrink-0">
                    <AlertCircle
                    className="h-5 w-5 text-red-400"
                    aria-hidden="true" />

                  </div>
                  <div className="ml-3">
                    <h3 className="text-sm font-medium text-red-800">
                      Erreur de connexion
                    </h3>
                    <div className="mt-2 text-sm text-red-700">
                      <p>{error}</p>
                    </div>
                  </div>
                </div>
              </div>
            }

            {mode === 'register' &&
            <div>
                <label
                htmlFor="firstName"
                className="block text-sm font-medium text-slate-700">

                  Prénom
                </label>
                <div className="mt-1 relative rounded-md shadow-sm">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <User
                    className="h-5 w-5 text-slate-400"
                    aria-hidden="true" />

                  </div>
                  <input
                  id="firstName"
                  name="firstName"
                  type="text"
                  autoComplete="given-name"
                  required
                  className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-10 sm:text-sm border-slate-300 rounded-md"
                  placeholder="Jean"
                  value={form.firstName}
                  onChange={(e) => setForm({ ...form, firstName: e.target.value })} />

                </div>
              </div>
            }

            {mode === 'register' &&
            <div>
                <label
                htmlFor="lastName"
                className="block text-sm font-medium text-slate-700">

                  Nom
                </label>
                <div className="mt-1 relative rounded-md shadow-sm">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <User className="h-5 w-5 text-slate-400" aria-hidden="true" />
                  </div>
                  <input
                  id="lastName"
                  name="lastName"
                  type="text"
                  autoComplete="family-name"
                  required
                  className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-10 sm:text-sm border-slate-300 rounded-md"
                  placeholder="Dupont"
                  value={form.lastName}
                  onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
                </div>
              </div>
            }

            <div>
              <label
                htmlFor="email"
                className="block text-sm font-medium text-slate-700">

                Adresse email
              </label>
              <div className="mt-1 relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Mail className="h-5 w-5 text-slate-400" aria-hidden="true" />
                </div>
                <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  required
                  className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-10 sm:text-sm border-slate-300 rounded-md"
                  placeholder="vous@exemple.com"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })} />

              </div>
            </div>

            {mode !== 'forgot' &&
            <div>
                <label
                htmlFor="password"
                className="block text-sm font-medium text-slate-700">

                  Mot de passe
                </label>
                <div className="mt-1 relative rounded-md shadow-sm">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Lock
                    className="h-5 w-5 text-slate-400"
                    aria-hidden="true" />

                  </div>
                  <input
                  id="password"
                  name="password"
                  type="password"
                  autoComplete="current-password"
                  required
                  className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-10 sm:text-sm border-slate-300 rounded-md"
                  placeholder="••••••••"
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })} />

                </div>
              </div>
            }

            {mode === 'register' &&
            <div>
                <label
                htmlFor="phoneNumber"
                className="block text-sm font-medium text-slate-700">

                  Téléphone
                </label>
                <div className="mt-1 relative rounded-md shadow-sm">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Phone className="h-5 w-5 text-slate-400" aria-hidden="true" />
                  </div>
                  <input
                  id="phoneNumber"
                  name="phoneNumber"
                  type="tel"
                  autoComplete="tel"
                  className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-10 sm:text-sm border-slate-300 rounded-md"
                  placeholder="+221700000000"
                  value={form.phoneNumber}
                  onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} />
                </div>
              </div>
            }

            <div>
              <Button
                type="submit"
                fullWidth
                disabled={isLoading}
                className="flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">

                {isLoading ?
                <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" /> :

                <>
                    {mode === 'login' && 'Se connecter'}
                    {mode === 'register' && 'Créer mon compte'}
                    {mode === 'forgot' && 'Envoyer le lien'}
                    <ArrowRight className="ml-2 h-4 w-4" />
                  </>
                }
              </Button>
            </div>
          </form>

          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-slate-300" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-slate-500">
                  Ou continuer avec
                </span>
              </div>
            </div>

            <div className="mt-6 grid grid-cols-2 gap-3">
              <button className="w-full inline-flex justify-center py-2 px-4 border border-slate-300 rounded-md shadow-sm bg-white text-sm font-medium text-slate-500 hover:bg-slate-50">
                <span className="sr-only">Google</span>
                <svg
                  className="h-5 w-5"
                  fill="currentColor"
                  viewBox="0 0 24 24">

                  <path d="M12.545,10.239v3.821h5.445c-0.712,2.315-2.647,3.972-5.445,3.972c-3.332,0-6.033-2.701-6.033-6.032s2.701-6.032,6.033-6.032c1.498,0,2.866,0.549,3.921,1.453l2.814-2.814C17.503,2.988,15.139,2,12.545,2C7.021,2,2.543,6.477,2.543,12s4.478,10,10.002,10c8.396,0,10.249-7.85,9.426-11.748L12.545,10.239z" />
                </svg>
              </button>
              <button className="w-full inline-flex justify-center py-2 px-4 border border-slate-300 rounded-md shadow-sm bg-white text-sm font-medium text-slate-500 hover:bg-slate-50">
                <span className="sr-only">Apple</span>
                <svg
                  className="h-5 w-5"
                  fill="currentColor"
                  viewBox="0 0 24 24">

                  <path d="M17.05 20.28c-.98.95-2.05.8-3.08.35-1.09-.46-2.09-.48-3.24 0-1.44.62-2.2.44-3.06-.35C2.79 15.25 3.51 7.59 9.05 7.31c1.35.07 2.29.74 3.08.8 1.18-.24 2.31-.93 3.57-.84 1.51.12 2.65.72 3.4 1.8-3.1 1.88-2.5 5.75.1 6.74-.24.75-.59 1.52-1.15 2.47zM12.03 7.25c-.15-2.23 1.66-4.07 3.74-4.25.29 2.58-2.34 4.5-3.74 4.25z" />
                </svg>
              </button>
            </div>
          </div>

          <div className="mt-6 text-center text-sm">
            {mode === 'login' &&
            <>
                <button
                onClick={() => setMode('forgot')}
                className="font-medium text-blue-600 hover:text-blue-500">

                  Mot de passe oublié ?
                </button>
                <div className="mt-2">
                  Pas encore de compte ?{' '}
                  <button
                  onClick={() => setMode('register')}
                  className="font-medium text-blue-600 hover:text-blue-500">

                    S'inscrire gratuitement
                  </button>
                </div>
              </>
            }
            {mode === 'register' &&
            <div>
                Déjà inscrit ?{' '}
                <button
                onClick={() => setMode('login')}
                className="font-medium text-blue-600 hover:text-blue-500">

                  Se connecter
                </button>
              </div>
            }
            {mode === 'forgot' &&
            <div>
                <button
                onClick={() => setMode('login')}
                className="font-medium text-blue-600 hover:text-blue-500">

                  Retour à la connexion
                </button>
              </div>
            }
          </div>
        </Card>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4">
        <DisclaimerBanner />
      </div>
    </div>);

}