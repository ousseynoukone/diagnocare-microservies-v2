import React from 'react';
import { Activity, Menu, User, Globe } from 'lucide-react';
import { Button } from './ui/Button';
interface NavigationProps {
  currentPage: string;
  onNavigate: (page: string) => void;
  onToggleSidebar?: () => void;
  isAuthenticated?: boolean;
}
export function Navigation({
  currentPage,
  onNavigate,
  onToggleSidebar,
  isAuthenticated = false
}: NavigationProps) {
  // If authenticated, we show a simplified top bar because the Sidebar handles main nav
  if (isAuthenticated) {
    return (
      <nav className="bg-white border-b border-slate-200 sticky top-0 z-30 h-16 px-4 sm:px-6 lg:px-8 flex items-center justify-between lg:justify-end">
        <div className="lg:hidden flex items-center">
          <button
            onClick={onToggleSidebar}
            className="p-2 -ml-2 text-slate-500 hover:text-slate-700 rounded-md">

            <Menu className="w-6 h-6" />
          </button>
          <span className="ml-3 font-bold text-lg text-slate-900">
            DiagnoCare
          </span>
        </div>

        <div className="flex items-center gap-4">
          <button className="flex items-center gap-1 text-sm font-medium text-slate-600 hover:text-slate-900 px-2 py-1 rounded hover:bg-slate-50">
            <Globe className="w-4 h-4" />
            <span>FR</span>
          </button>
          <div className="h-6 w-px bg-slate-200 mx-1" />
          <button
            onClick={() => onNavigate('profile')}
            className="flex items-center gap-2 text-sm font-medium text-slate-700 hover:text-blue-600">

            <div className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center border border-slate-200">
              <User className="w-4 h-4 text-slate-500" />
            </div>
            <span className="hidden sm:block">Mon Compte</span>
          </button>
        </div>
      </nav>);

  }
  // Public Navigation (Landing Page)
  return (
    <nav className="bg-white/80 backdrop-blur-md border-b border-slate-200 sticky top-0 z-50">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div
            className="flex items-center cursor-pointer"
            onClick={() => onNavigate('landing')}>

            <div className="bg-[#1E40AF] p-1.5 rounded-md mr-2">
              <Activity className="h-5 w-5 text-white" />
            </div>
            <span className="font-bold text-xl text-slate-900 tracking-tight">
              DiagnoCare
            </span>
          </div>

          <div className="flex items-center gap-4">
            <button className="text-sm font-medium text-slate-600 hover:text-slate-900 hidden sm:block">
              Comment ça marche
            </button>
            <button className="text-sm font-medium text-slate-600 hover:text-slate-900 hidden sm:block">
              FAQ
            </button>
            <div className="h-6 w-px bg-slate-200 mx-2 hidden sm:block" />
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onNavigate('auth')}>

              Se connecter
            </Button>
            <Button size="sm" onClick={() => onNavigate('auth')}>
              Commencer
            </Button>
          </div>
        </div>
      </div>
    </nav>);

}