import React, { useState, useEffect, useCallback } from 'react';
import { Plus } from 'lucide-react';
import { ModuleRoute, MaterialTheme, ThemeMode } from './types';
import { MeridianStorage } from './services/storage';
import { applyThemeVariables } from './theme/materialYou';
import { Haptics } from './services/haptics';

// Common Components
import { Header } from './components/common/Header';
import { NavigationRail } from './components/common/NavigationRail';
import { CommandPalette } from './components/common/CommandPalette';
import { QuickAddModal } from './components/common/QuickAddModal';
import { SettingsModal } from './components/common/SettingsModal';
import { DayDetailModal } from './components/common/DayDetailModal';

// Views
import { OverviewView } from './components/views/OverviewView';
import { TimelineView } from './components/views/TimelineView';
import { JournalView } from './components/views/JournalView';
import { StudyView } from './components/views/StudyView';
import { RecoveryView } from './components/views/RecoveryView';
import { FinanceView } from './components/views/FinanceView';
import { PulseView } from './components/views/PulseView';
import { GoalsView } from './components/views/GoalsView';

export default function App() {
  // Navigation Route (checks URL search param for Android home-screen shortcuts)
  const [currentRoute, setCurrentRoute] = useState<ModuleRoute>(() => {
    try {
      const urlParams = new URLSearchParams(window.location.search);
      const routeParam = urlParams.get('route') as ModuleRoute;
      const validRoutes: ModuleRoute[] = ['overview', 'timeline', 'journal', 'study', 'recovery', 'finance', 'checkin', 'goals'];
      if (routeParam && validRoutes.includes(routeParam)) {
        return routeParam;
      }
    } catch {}
    return 'overview';
  });

  // Theme configuration
  const [themePalette, setThemePalette] = useState<MaterialTheme>(() => MeridianStorage.getThemeConfig().palette);
  const [themeMode, setThemeMode] = useState<ThemeMode>(() => MeridianStorage.getThemeConfig().mode);

  // Modals state
  const [isCommandPaletteOpen, setIsCommandPaletteOpen] = useState(false);
  const [isQuickAddOpen, setIsQuickAddOpen] = useState(false);
  const [isSettingsModalOpen, setIsSettingsModalOpen] = useState(false);
  const [selectedDayDetailDate, setSelectedDayDetailDate] = useState<string | null>(null);
  const [timelineInitialTag, setTimelineInitialTag] = useState<string | null>(null);

  // Re-render tick when storage changes
  const [dataVersion, setDataVersion] = useState(0);
  const triggerDataRefresh = useCallback(() => setDataVersion(v => v + 1), []);

  // Dynamic Navigation Badges
  const badges = React.useMemo(() => {
    try {
      const journalCount = MeridianStorage.getJournal().length;
      const recoveryState = MeridianStorage.getRecovery();
      const firstQuit = recoveryState.quits?.[0];
      const daysClean = firstQuit
        ? Math.max(0, Math.floor((Date.now() - firstQuit.quitTimestamp) / (1000 * 60 * 60 * 24)))
        : 0;
      const goalsState = MeridianStorage.getGoals();
      const activeGoals = (goalsState.goals || []).filter(g => !g.archived).length;

      return {
        journal: journalCount > 0 ? journalCount : '',
        recovery: daysClean > 0 ? `${daysClean}d` : '',
        goals: activeGoals > 0 ? activeGoals : '',
      };
    } catch {
      return {};
    }
  }, [dataVersion]);

  // Apply Material You theme variables & update Android status bar meta tag
  useEffect(() => {
    applyThemeVariables(themePalette, themeMode);
    MeridianStorage.saveThemeConfig({ palette: themePalette, mode: themeMode });

    // Update Android system status bar theme-color
    const metaThemeColor = document.querySelector('meta[name="theme-color"]');
    if (metaThemeColor) {
      metaThemeColor.setAttribute('content', themeMode === 'dark' ? '#111412' : '#F9FAF7');
    }
  }, [themePalette, themeMode]);

  // Android hardware / gesture back button handler
  useEffect(() => {
    const isAnyModalOpen =
      isCommandPaletteOpen ||
      isQuickAddOpen ||
      isSettingsModalOpen ||
      Boolean(selectedDayDetailDate);

    // Push history entry when opening modal so Android back gesture closes modal first
    if (isAnyModalOpen) {
      window.history.pushState({ modalOpen: true }, '');
    }

    const handlePopState = () => {
      if (isCommandPaletteOpen) setIsCommandPaletteOpen(false);
      else if (isQuickAddOpen) setIsQuickAddOpen(false);
      else if (isSettingsModalOpen) setIsSettingsModalOpen(false);
      else if (selectedDayDetailDate) setSelectedDayDetailDate(null);
      else if (currentRoute !== 'overview') {
        setCurrentRoute('overview');
      }
    };

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, [
    isCommandPaletteOpen,
    isQuickAddOpen,
    isSettingsModalOpen,
    selectedDayDetailDate,
    currentRoute,
  ]);

  // Global keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Cmd/Ctrl + K => Command Palette
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        setIsCommandPaletteOpen(prev => !prev);
      }
      // 'q' when no input is focused => Quick Add
      else if (
        e.key === 'q' &&
        !['INPUT', 'TEXTAREA'].includes((e.target as HTMLElement)?.tagName) &&
        !isCommandPaletteOpen &&
        !isQuickAddOpen &&
        !isSettingsModalOpen
      ) {
        e.preventDefault();
        setIsQuickAddOpen(true);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isCommandPaletteOpen, isQuickAddOpen, isSettingsModalOpen]);

  const handleSelectTheme = (palette: MaterialTheme, mode: ThemeMode) => {
    setThemePalette(palette);
    setThemeMode(mode);
  };

  const handleOpenTimelineWithTag = (tag: string) => {
    setTimelineInitialTag(tag);
    setCurrentRoute('timeline');
  };

  const handleFabClick = () => {
    Haptics.medium();
    setIsQuickAddOpen(true);
  };

  return (
    <div
      className="min-h-screen flex flex-col md:flex-row font-sans transition-colors duration-150"
      style={{
        backgroundColor: 'var(--md-sys-color-surface)',
        color: 'var(--md-sys-color-on-surface)',
      }}
    >
      {/* 1. Desktop Left Navigation Rail & Mobile Bottom Bar */}
      <NavigationRail
        currentRoute={currentRoute}
        onNavigate={setCurrentRoute}
        onOpenSettings={() => setIsSettingsModalOpen(true)}
        badges={badges}
      />

      {/* 2. Main Content Column */}
      <div className="flex-1 flex flex-col min-w-0 min-h-screen pb-24 md:pb-8">
        {/* Top Header */}
        <Header
          currentRoute={currentRoute}
          onOpenCommandPalette={() => setIsCommandPaletteOpen(true)}
          onOpenSettings={() => setIsSettingsModalOpen(true)}
        />

        {/* View Surface Area - Snappy & Instant */}
        <main className="flex-1 p-4 md:p-6 lg:p-8 max-w-7xl w-full mx-auto">
          <div key={currentRoute} className="m3-fade-enter">
            {currentRoute === 'overview' && (
              <OverviewView
                onNavigate={setCurrentRoute}
                onOpenDayDetail={date => setSelectedDayDetailDate(date)}
                onOpenTimelineWithTag={handleOpenTimelineWithTag}
              />
            )}

            {currentRoute === 'timeline' && (
              <TimelineView
                initialTagFilter={timelineInitialTag}
                onNavigate={setCurrentRoute}
                onOpenDayDetail={date => setSelectedDayDetailDate(date)}
                onOpenQuickAdd={() => setIsQuickAddOpen(true)}
              />
            )}

            {currentRoute === 'journal' && <JournalView />}

            {currentRoute === 'study' && <StudyView />}

            {currentRoute === 'recovery' && <RecoveryView />}

            {currentRoute === 'finance' && <FinanceView />}

            {currentRoute === 'checkin' && <PulseView />}

            {currentRoute === 'goals' && <GoalsView />}
          </div>
        </main>
      </div>

      {/* ═══════════════════════════════════════════════════════════════
          MATERIAL 3 FLOATING ACTION BUTTON (FAB)
          Canonical entry point for logging without repetitive buttons
          ═══════════════════════════════════════════════════════════════ */}
      <button
        onClick={handleFabClick}
        type="button"
        className="fixed bottom-20 md:bottom-8 right-5 md:right-8 z-30 flex items-center gap-2 px-4 py-3.5 md:px-5 md:py-4 rounded-2xl md:rounded-3xl shadow-lg border border-[var(--md-sys-color-outline-variant)] m3-pressable transition-all hover:scale-105 active:scale-95 group"
        style={{
          backgroundColor: 'var(--md-sys-color-primary-container)',
          color: 'var(--md-sys-color-on-primary-container)',
        }}
        aria-label="Quick Add Entry"
      >
        <Plus className="w-5 h-5 md:w-6 md:h-6 transition-transform group-hover:rotate-90 duration-200" />
        <span className="font-bold text-xs md:text-sm font-display tracking-wide">
          Log Entry
        </span>
      </button>

      {/* 3. Global Modals */}
      <CommandPalette
        isOpen={isCommandPaletteOpen}
        onClose={() => setIsCommandPaletteOpen(false)}
        onNavigate={setCurrentRoute}
        onOpenQuickAdd={() => setIsQuickAddOpen(true)}
        onOpenSettings={() => setIsSettingsModalOpen(true)}
      />

      <QuickAddModal
        isOpen={isQuickAddOpen}
        onClose={() => setIsQuickAddOpen(false)}
        onEntrySaved={triggerDataRefresh}
      />

      <SettingsModal
        isOpen={isSettingsModalOpen}
        onClose={() => setIsSettingsModalOpen(false)}
        currentPalette={themePalette}
        currentMode={themeMode}
        onSelectTheme={handleSelectTheme}
        onDataChanged={triggerDataRefresh}
      />

      <DayDetailModal
        date={selectedDayDetailDate}
        onClose={() => setSelectedDayDetailDate(null)}
        onNavigate={setCurrentRoute}
        onRefresh={triggerDataRefresh}
      />
    </div>
  );
}
