import React from 'react';
import {
  Home,
  Activity,
  Clock,
  FileText,
  ClipboardList,
  User,
  Settings,
  LogOut,
  X } from
'lucide-react';
interface SidebarProps {
  currentPage: string;
  onNavigate: (page: string) => void;
  isOpen: boolean;
  onClose: () => void;
}
export function Sidebar({
  currentPage,
  onNavigate,
  isOpen,
  onClose
}: SidebarProps) {
  const menuItems = [
  {
    id: 'dashboard',
    label: 'Accueil',
    icon: Home
  },
  {
    id: 'evaluation',
    label: 'Évaluation',
    icon: Activity
  },
  {
    id: 'followup',
    label: 'Suivis',
    icon: Clock
  },
  {
    id: 'history',
    label: 'Historique',
    icon: FileText
  },
  {
    id: 'summary',
    label: 'Résumé',
    icon: ClipboardList
  },
  {
    id: 'profile',
    label: 'Profil médical',
    icon: User
  },
  {
    id: 'settings',
    label: 'Paramètres',
    icon: Settings
  }];

  return (
    <>
      {/* Mobile Overlay */}
      {isOpen &&
      <div
        className="fixed inset-0 bg-slate-900/50 z-40 lg:hidden backdrop-blur-sm"
        onClick={onClose} />

      }

      {/* Sidebar Container */}
      <aside
        className={`
        fixed top-0 left-0 z-50 h-full w-64 bg-slate-900 text-white transform transition-transform duration-300 ease-in-out
        ${isOpen ? 'translate-x-0' : '-translate-x-full'}
        lg:translate-x-0 lg:static lg:block flex-shrink-0
      `}>

        <div className="h-full flex flex-col">
          {/* Header */}
          <div className="h-16 flex items-center justify-between px-6 border-b border-slate-800">
            <div className="flex items-center gap-2">
              <div className="bg-blue-600 p-1.5 rounded-md">
                <Activity className="h-5 w-5 text-white" />
              </div>
              <span className="font-bold text-xl tracking-tight">
                DiagnoCare
              </span>
            </div>
            <button
              onClick={onClose}
              className="lg:hidden text-slate-400 hover:text-white">

              <X className="w-6 h-6" />
            </button>
          </div>

          {/* Navigation */}
          <nav className="flex-1 px-3 py-6 space-y-1 overflow-y-auto">
            {menuItems.map((item) => {
              const Icon = item.icon;
              const isActive = currentPage === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => {
                    onNavigate(item.id);
                    onClose();
                  }}
                  className={`
                    w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors
                    ${isActive ? 'bg-blue-600 text-white shadow-lg shadow-blue-900/20' : 'text-slate-400 hover:text-white hover:bg-slate-800'}
                  `}>

                  <Icon
                    className={`w-5 h-5 ${isActive ? 'text-white' : 'text-slate-400 group-hover:text-white'}`} />

                  {item.label}
                </button>);

            })}
          </nav>

          {/* Footer / User */}
          <div className="p-4 border-t border-slate-800">
            <div className="flex items-center gap-3 mb-4 px-2">
              <div className="w-8 h-8 rounded-full bg-blue-900 flex items-center justify-center text-xs font-bold text-blue-100 border border-blue-700">
                JD
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-white truncate">
                  Jean Dupont
                </p>
                <p className="text-xs text-slate-500 truncate">
                  jean.dupont@email.com
                </p>
              </div>
            </div>
            <button
              onClick={() => onNavigate('landing')}
              className="w-full flex items-center gap-2 px-3 py-2 text-sm font-medium text-red-400 hover:text-red-300 hover:bg-red-900/20 rounded-lg transition-colors">

              <LogOut className="w-4 h-4" />
              Déconnexion
            </button>
          </div>
        </div>
      </aside>
    </>);

}