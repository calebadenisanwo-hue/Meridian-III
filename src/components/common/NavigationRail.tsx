import React from 'react';
import {
  LayoutDashboard,
  Clock,
  BookOpen,
  GraduationCap,
  ShieldCheck,
  Wallet,
  Activity,
  Target,
  Sparkles,
  Plus,
} from 'lucide-react';
import { ModuleRoute } from '../../types';
import { Haptics } from '../../services/haptics';

interface NavigationRailProps {
  currentRoute: ModuleRoute;
  onNavigate: (route: ModuleRoute) => void;
  onOpenQuickAdd?: () => void;
  badges?: Record<string, string | number>;
}

const NAV_ITEMS: {
  route: ModuleRoute;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
  accentColor: string;
}[] = [
  { route: 'overview', label: 'Overview', icon: LayoutDashboard, accentColor: 'var(--md-sys-color-primary)' },
  { route: 'timeline', label: 'Timeline', icon: Clock, accentColor: '#C77DFF' },
  { route: 'journal', label: 'Journal', icon: BookOpen, accentColor: '#2D6A4F' },
  { route: 'study', label: 'Study', icon: GraduationCap, accentColor: '#22A566' },
  { route: 'recovery', label: 'Unbound', icon: ShieldCheck, accentColor: '#D3A346' },
  { route: 'finance', label: 'Finance', icon: Wallet, accentColor: '#4FA9E0' },
  { route: 'checkin', label: 'Pulse', icon: Activity, accentColor: '#F0A8C4' },
  { route: 'goals', label: 'Goals', icon: Target, accentColor: '#E8B368' },
];

export const NavigationRail: React.FC<NavigationRailProps> = ({
  currentRoute,
  onNavigate,
  onOpenQuickAdd,
  badges = {},
}) => {
  const handleNavClick = (route: ModuleRoute) => {
    Haptics.selection();
    onNavigate(route);
  };

  const handleQuickAddClick = () => {
    Haptics.light();
    onOpenQuickAdd?.();
  };

  return (
    <>
      {/* Desktop Navigation Rail */}
      <aside
        className="hidden md:flex flex-col w-64 h-screen sticky top-0 border-r shrink-0 z-40 select-none transition-colors"
        style={{
          backgroundColor: 'var(--md-sys-color-surface-container-low)',
          borderColor: 'var(--md-sys-color-outline-variant)',
        }}
      >
        {/* Brand */}
        <div className="p-5 pb-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div
              className="w-10 h-10 rounded-2xl flex items-center justify-center shadow-md relative overflow-hidden"
              style={{
                background: 'linear-gradient(135deg, var(--md-sys-color-primary) 0%, var(--md-sys-color-secondary) 100%)',
                color: 'var(--md-sys-color-on-primary)',
              }}
            >
              <Sparkles className="w-5 h-5 animate-pulse" />
            </div>
            <div>
              <div className="text-base font-bold font-display tracking-tight text-on-surface flex items-center gap-1.5">
                <span>Meridian</span>
              </div>
              <div className="text-[11px] font-mono tracking-wider uppercase text-on-surface-variant">
                Personal Systems
              </div>
            </div>
          </div>
        </div>

        {/* Quick Add Action Button in Rail */}
        {onOpenQuickAdd && (
          <div className="px-3 pb-2">
            <button
              type="button"
              onClick={handleQuickAddClick}
              className="w-full flex items-center justify-center gap-2 px-4 py-2.5 rounded-2xl font-semibold text-xs transition-all shadow-sm active:scale-[0.98] hover:shadow"
              style={{
                backgroundColor: 'var(--md-sys-color-primary)',
                color: 'var(--md-sys-color-on-primary)',
              }}
            >
              <Plus className="w-4 h-4" />
              <span>Quick Log (Q)</span>
            </button>
          </div>
        )}

        {/* Divider */}
        <div
          className="h-[2px] mx-5 my-1 rounded-full opacity-60"
          style={{
            background: 'linear-gradient(90deg, var(--md-sys-color-primary), var(--md-sys-color-tertiary), var(--md-sys-color-secondary))',
          }}
        />

        {/* Navigation Items */}
        <nav className="flex-1 px-3 py-3 space-y-1 overflow-y-auto">
          {NAV_ITEMS.map(item => {
            const Icon = item.icon;
            const isActive = currentRoute === item.route;
            const badgeValue = badges?.[item.route];

            return (
              <button
                key={item.route}
                type="button"
                onClick={() => handleNavClick(item.route)}
                className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-full text-xs md:text-sm font-medium transition-all group relative m3-ripple ${
                  isActive ? 'shadow-sm font-semibold' : 'hover:bg-black/5 dark:hover:bg-white/5'
                }`}
                style={{
                  backgroundColor: isActive ? 'var(--md-sys-color-primary-container)' : 'transparent',
                  color: isActive ? 'var(--md-sys-color-on-primary-container)' : 'var(--md-sys-color-on-surface-variant)',
                }}
              >
                {/* Active Indicator Bar */}
                {isActive && (
                  <span
                    className="absolute left-1.5 top-1/2 -translate-y-1/2 w-1.5 h-6 rounded-full"
                    style={{ backgroundColor: 'var(--md-sys-color-primary)' }}
                  />
                )}

                {/* Icon in container */}
                <div
                  className={`w-8 h-8 rounded-full flex items-center justify-center transition-all ${
                    isActive ? 'scale-105' : 'group-hover:scale-105'
                  }`}
                  style={{
                    backgroundColor: isActive ? 'var(--md-sys-color-primary)' : 'transparent',
                    color: isActive ? 'var(--md-sys-color-on-primary)' : 'inherit',
                  }}
                >
                  <Icon className="w-4 h-4" />
                </div>

                <span className="truncate flex-1 text-left">{item.label}</span>

                {/* Badge if present */}
                {badgeValue !== undefined && badgeValue !== '' && (
                  <span
                    className="px-2 py-0.5 text-[10.5px] font-mono rounded-full font-semibold shrink-0"
                    style={{
                      backgroundColor: isActive ? 'var(--md-sys-color-primary)' : 'var(--md-sys-color-surface-container-high)',
                      color: isActive ? 'var(--md-sys-color-on-primary)' : 'var(--md-sys-color-on-surface-variant)',
                    }}
                  >
                    {badgeValue}
                  </span>
                )}
              </button>
            );
          })}
        </nav>

        {/* Footer info */}
        <div
          className="p-4 border-t text-[11px] text-on-surface-variant space-y-1"
          style={{ borderColor: 'var(--md-sys-color-outline-variant)' }}
        >
          <div className="flex items-center justify-between font-mono">
            <span>Systems Online</span>
            <span className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-ping inline-block" />
              <span className="text-emerald-400 font-semibold">6 Modules</span>
            </span>
          </div>
          <p className="text-[10px] text-on-surface-variant/80">Offline-first · Material You v3</p>
        </div>
      </aside>

      {/* Mobile Bottom Navigation Bar (Material 3 style with gesture pill safe-area) */}
      <div
        className="md:hidden fixed bottom-0 left-0 right-0 z-40 border-t backdrop-blur-xl px-1.5 pt-1.5 pb-[calc(0.5rem+env(safe-area-inset-bottom,0px))] flex items-center justify-around select-none"
        style={{
          backgroundColor: 'var(--md-sys-color-surface-container-high, rgba(30, 32, 28, 0.95))',
          borderColor: 'var(--md-sys-color-outline-variant)',
        }}
      >
        {NAV_ITEMS.map(item => {
          const Icon = item.icon;
          const isActive = currentRoute === item.route;

          return (
            <button
              key={item.route}
              type="button"
              onClick={() => handleNavClick(item.route)}
              className="flex flex-col items-center gap-0.5 px-2 py-1 relative active:scale-95 transition-transform"
              style={{
                color: isActive ? 'var(--md-sys-color-primary)' : 'var(--md-sys-color-on-surface-variant)',
              }}
            >
              <div
                className={`px-3 py-1 rounded-full transition-all flex items-center justify-center ${
                  isActive ? 'scale-105 shadow-sm' : ''
                }`}
                style={{
                  backgroundColor: isActive ? 'var(--md-sys-color-primary-container)' : 'transparent',
                  color: isActive ? 'var(--md-sys-color-on-primary-container)' : 'inherit',
                }}
              >
                <Icon className="w-4 h-4" />
              </div>
              <span className="text-[9.5px] font-medium leading-none tracking-tight">{item.label}</span>
            </button>
          );
        })}
      </div>
    </>
  );
};
