import React from 'react';
import {
  Stethoscope,
  ShieldCheck,
  Zap,
  ArrowRight,
  UserCheck,
  FileText,
  Activity } from
'lucide-react';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';
import { DisclaimerBanner } from '../components/DisclaimerBanner';
import { FAQAccordion } from '../components/FAQAccordion';
import { Footer } from '../components/Footer';
interface LandingPageProps {
  onStart: () => void;
}
export function LandingPage({ onStart }: LandingPageProps) {
  return (
    <div className="min-h-screen flex flex-col bg-slate-50">
      {/* Hero Section */}
      <main className="flex-grow">
        <div className="bg-white border-b border-slate-200">
          <div className="max-w-5xl mx-auto px-4 py-16 sm:py-24 lg:py-32 text-center">
            <div className="flex justify-center mb-8">
              <div className="bg-blue-50 p-4 rounded-full ring-8 ring-blue-50/50 animate-pulse">
                <Stethoscope className="h-16 w-16 text-[#1E40AF]" />
              </div>
            </div>
            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-slate-900 tracking-tight mb-6">
              Votre santé, éclairée par <br className="hidden sm:block" />
              <span className="text-[#1E40AF]">
                l'intelligence artificielle
              </span>
            </h1>
            <p className="text-lg sm:text-xl text-slate-600 max-w-2xl mx-auto mb-10 leading-relaxed">
              Une analyse de symptômes rapide, précise et confidentielle pour
              vous guider vers les bons soins. Technologie médicale avancée,
              interface simplifiée pour tous.
            </p>
            <div className="flex flex-col sm:flex-row justify-center gap-4">
              <Button
                size="lg"
                onClick={onStart}
                className="gap-2 shadow-lg shadow-blue-900/20">

                Commencer une évaluation
                <ArrowRight className="w-5 h-5" />
              </Button>
              <Button variant="secondary" size="lg">
                En savoir plus
              </Button>
            </div>
            <div className="mt-8 flex items-center justify-center gap-2 text-sm text-slate-500">
              <ShieldCheck className="w-4 h-4 text-emerald-600" />
              <span>Données chiffrées & sécurisées (HDS)</span>
            </div>
          </div>
        </div>

        {/* Value Props */}
        <div className="bg-slate-50 py-24">
          <div className="max-w-5xl mx-auto px-4">
            <div className="text-center mb-16">
              <h2 className="text-3xl font-bold text-slate-900">
                Pourquoi choisir DiagnoCare ?
              </h2>
              <p className="mt-4 text-lg text-slate-600">
                Une approche médicale rigoureuse alliée à la puissance de l'IA.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
              <Card className="h-full hover:shadow-lg transition-all duration-300 hover:-translate-y-1">
                <div className="h-12 w-12 bg-blue-100 rounded-xl flex items-center justify-center mb-6">
                  <Zap className="h-6 w-6 text-[#1E40AF]" />
                </div>
                <h3 className="text-xl font-bold text-slate-900 mb-3">
                  Analyse Intelligente
                </h3>
                <p className="text-slate-600 leading-relaxed">
                  Nos algorithmes sont entraînés sur des millions de cas
                  cliniques validés pour offrir une précision maximale dans
                  l'identification des pathologies.
                </p>
              </Card>

              <Card className="h-full hover:shadow-lg transition-all duration-300 hover:-translate-y-1">
                <div className="h-12 w-12 bg-emerald-100 rounded-xl flex items-center justify-center mb-6">
                  <Activity className="h-6 w-6 text-emerald-700" />
                </div>
                <h3 className="text-xl font-bold text-slate-900 mb-3">
                  Suivi d'Évolution
                </h3>
                <p className="text-slate-600 leading-relaxed">
                  Ne restez pas seul face à vos symptômes. DiagnoCare assure un
                  suivi régulier (24h/48h) pour détecter toute aggravation ou
                  complication.
                </p>
              </Card>

              <Card className="h-full hover:shadow-lg transition-all duration-300 hover:-translate-y-1">
                <div className="h-12 w-12 bg-purple-100 rounded-xl flex items-center justify-center mb-6">
                  <ShieldCheck className="h-6 w-6 text-purple-700" />
                </div>
                <h3 className="text-xl font-bold text-slate-900 mb-3">
                  Confidentialité Totale
                </h3>
                <p className="text-slate-600 leading-relaxed">
                  Vos données de santé sont chiffrées de bout en bout. Elles ne
                  quittent jamais votre appareil sans votre consentement
                  explicite.
                </p>
              </Card>
            </div>
          </div>
        </div>

        {/* How it works */}
        <div className="bg-white py-24 border-y border-slate-200">
          <div className="max-w-5xl mx-auto px-4">
            <div className="text-center mb-16">
              <h2 className="text-3xl font-bold text-slate-900">
                Comment ça marche ?
              </h2>
              <p className="mt-4 text-lg text-slate-600">
                Un parcours simple en 4 étapes pour prendre soin de vous.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-8 relative">
              {/* Connecting line for desktop */}
              <div className="hidden md:block absolute top-12 left-0 right-0 h-0.5 bg-slate-100 -z-10" />

              {[
              {
                icon: UserCheck,
                title: '1. Décrivez',
                desc: 'Saisissez vos symptômes en langage naturel ou via notre liste guidée.'
              },
              {
                icon: Zap,
                title: '2. Analysez',
                desc: 'Notre IA compare votre profil à des milliers de cas similaires instantanément.'
              },
              {
                icon: FileText,
                title: '3. Comprenez',
                desc: 'Recevez un rapport détaillé avec les causes probables et conseils.'
              },
              {
                icon: Stethoscope,
                title: '4. Consultez',
                desc: 'Soyez orienté vers le bon spécialiste avec un résumé médical prêt.'
              }].
              map((step, idx) =>
              <div
                key={idx}
                className="flex flex-col items-center text-center group">

                  <div className="w-24 h-24 bg-white border-4 border-blue-50 rounded-full flex items-center justify-center mb-6 group-hover:border-blue-100 transition-colors shadow-sm">
                    <step.icon className="w-10 h-10 text-blue-600" />
                  </div>
                  <h3 className="text-lg font-bold text-slate-900 mb-2">
                    {step.title}
                  </h3>
                  <p className="text-sm text-slate-500">{step.desc}</p>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* FAQ Section */}
        <div className="bg-slate-50 py-24">
          <div className="max-w-3xl mx-auto px-4">
            <div className="text-center mb-12">
              <h2 className="text-3xl font-bold text-slate-900">
                Questions fréquentes
              </h2>
            </div>
            <FAQAccordion />
          </div>
        </div>

        {/* CTA Section */}
        <div className="bg-[#1E40AF] py-16">
          <div className="max-w-4xl mx-auto px-4 text-center">
            <h2 className="text-3xl font-bold text-white mb-6">
              Prêt à prendre votre santé en main ?
            </h2>
            <p className="text-blue-100 text-lg mb-8 max-w-2xl mx-auto">
              Rejoignez les milliers de patients qui utilisent DiagnoCare pour
              mieux comprendre leurs symptômes au quotidien.
            </p>
            <Button
              size="lg"
              onClick={onStart}
              className="bg-white text-blue-900 hover:bg-blue-50 border-none shadow-xl">

              Lancer une analyse gratuite
            </Button>
          </div>
        </div>
      </main>

      <DisclaimerBanner />
      <Footer />
    </div>);

}