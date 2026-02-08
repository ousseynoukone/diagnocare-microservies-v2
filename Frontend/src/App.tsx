import React, { useEffect, useState } from 'react';
import { tokenStorage } from './api/storage';
import { Navigation } from './components/Navigation';
import { Sidebar } from './components/Sidebar';
import { LandingPage } from './pages/LandingPage';
import { AuthPage } from './pages/AuthPage';
import { DashboardPage } from './pages/DashboardPage';
import { SymptomEvaluationPage } from './pages/SymptomEvaluationPage';
import { PredictionResultPage } from './pages/PredictionResultPage';
import { SpecialistFinderPage } from './pages/SpecialistFinderPage';
import { FollowUpPage } from './pages/FollowUpPage';
import { TimelinePage } from './pages/TimelinePage';
import { HistoryPage } from './pages/HistoryPage';
import { SummaryPage } from './pages/SummaryPage';
import { MedicalProfilePage } from './pages/MedicalProfilePage';
import { SettingsPage } from './pages/SettingsPage';
export function App() {
  const [currentPage, setCurrentPage] = useState('landing');
  const [isAuthenticated, setIsAuthenticated] = useState(
    Boolean(tokenStorage.getAccessToken())
  );
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  // Scroll to top on page change
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [currentPage]);
  const handleLogin = () => {
    setIsAuthenticated(true);
    setCurrentPage('dashboard');
  };
  const handleLogout = () => {
    tokenStorage.clear();
    setIsAuthenticated(false);
    setCurrentPage('landing');
    setIsSidebarOpen(false);
  };
  const renderPage = () => {
    switch (currentPage) {
      case 'landing':
        return <LandingPage onStart={() => setCurrentPage('auth')} />;
      case 'auth':
        return <AuthPage onLogin={handleLogin} />;
      case 'dashboard':
        return (
          <DashboardPage
            onNewEvaluation={() => setCurrentPage('evaluation')}
            onNavigate={setCurrentPage} />);


      case 'evaluation':
        return <SymptomEvaluationPage onNavigate={setCurrentPage} />;
      case 'results':
        return (
          <PredictionResultPage
            onBack={() => setCurrentPage('evaluation')}
            onNavigate={setCurrentPage} />);


      case 'specialist-finder':
        return <SpecialistFinderPage onBack={() => setCurrentPage('results')} />;
      case 'followup':
        return <FollowUpPage onNavigate={setCurrentPage} />;
      case 'timeline':
        return <TimelinePage onNavigate={setCurrentPage} />;
      case 'history':
        return <HistoryPage onNavigate={setCurrentPage} />;
      case 'summary':
        return <SummaryPage />;
      case 'profile':
        return <MedicalProfilePage />;
      case 'settings':
        return <SettingsPage onLogout={handleLogout} />;
      default:
        return <LandingPage onStart={() => setCurrentPage('auth')} />;
    }
  };
  // Layout Logic
  const isPublicPage = ['landing', 'auth'].includes(currentPage);
  if (isPublicPage) {
    return (
      <div className="min-h-screen bg-slate-50 font-sans text-slate-900">
        <Navigation
          currentPage={currentPage}
          onNavigate={setCurrentPage}
          isAuthenticated={false} />

        {renderPage()}
      </div>);

  }
  // Authenticated App Layout
  return (
    <div className="min-h-screen bg-slate-50 font-sans text-slate-900 flex">
      {/* Sidebar */}
      <Sidebar
        currentPage={currentPage}
        onNavigate={setCurrentPage}
        isOpen={isSidebarOpen}
        onClose={() => setIsSidebarOpen(false)} />


      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0">
        <Navigation
          currentPage={currentPage}
          onNavigate={setCurrentPage}
          isAuthenticated={true}
          onToggleSidebar={() => setIsSidebarOpen(!isSidebarOpen)} />

        <main className="flex-1 overflow-y-auto">{renderPage()}</main>
      </div>
    </div>);

}