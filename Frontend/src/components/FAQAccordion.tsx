import React, { useState } from 'react';
import { ChevronDown, ChevronUp } from 'lucide-react';
interface FAQItemProps {
  question: string;
  answer: string;
}
function FAQItem({ question, answer }: FAQItemProps) {
  const [isOpen, setIsOpen] = useState(false);
  return (
    <div className="border-b border-slate-200 last:border-0">
      <button
        className="w-full py-4 flex items-center justify-between text-left focus:outline-none"
        onClick={() => setIsOpen(!isOpen)}>

        <span className="font-medium text-slate-900 pr-8">{question}</span>
        {isOpen ?
        <ChevronUp className="w-5 h-5 text-blue-600 flex-shrink-0" /> :

        <ChevronDown className="w-5 h-5 text-slate-400 flex-shrink-0" />
        }
      </button>
      {isOpen &&
      <div className="pb-4 text-slate-600 leading-relaxed animate-in slide-in-from-top-2 duration-200">
          {answer}
        </div>
      }
    </div>);

}
export function FAQAccordion() {
  const faqs = [
  {
    question: 'Est-ce un diagnostic médical ?',
    answer:
    "Non. DiagnoCare est un outil d'aide à la décision basé sur l'intelligence artificielle. Il fournit des probabilités et des recommandations, mais ne remplace en aucun cas l'avis d'un médecin. En cas de doute, consultez toujours un professionnel de santé."
  },
  {
    question: 'Mes données sont-elles sécurisées ?',
    answer:
    'Oui, absolument. Vos données sont chiffrées de bout en bout et stockées sur des serveurs sécurisés certifiés HDS (Hébergeur de Données de Santé). Nous ne partageons jamais vos informations personnelles sans votre consentement explicite.'
  },
  {
    question: "Comment fonctionne l'IA ?",
    answer:
    'Notre IA a été entraînée sur des millions de cas cliniques anonymisés et validée par un comité médical. Elle analyse vos symptômes pour identifier des motifs (patterns) correspondant à des pathologies connues.'
  },
  {
    question: 'Puis-je partager les résultats avec mon médecin ?',
    answer:
    'Oui, vous pouvez générer un résumé PDF complet de votre évaluation et de votre suivi, conçu spécifiquement pour être transmis à votre médecin lors de votre consultation.'
  },
  {
    question: 'Combien ça coûte ?',
    answer:
    "L'application est gratuite pour les patients. Certaines fonctionnalités avancées de suivi à long terme peuvent faire l'objet d'un abonnement premium optionnel."
  }];

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
      <div className="p-6 sm:p-8">
        {faqs.map((faq, index) =>
        <FAQItem key={index} question={faq.question} answer={faq.answer} />
        )}
      </div>
    </div>);

}