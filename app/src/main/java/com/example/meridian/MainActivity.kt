package com.example.meridian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.meridian.data.model.ModuleRoute
import com.example.meridian.ui.components.*
import com.example.meridian.ui.screens.*
import com.example.meridian.ui.theme.MeridianTheme
import com.example.meridian.ui.viewmodel.MeridianViewModel
import com.example.meridian.ui.viewmodel.MeridianViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: MeridianViewModel by viewModels {
        MeridianViewModelFactory((application as MeridianApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            var showCommandPalette by remember { mutableStateOf(false) }
            var showThemeDialog by remember { mutableStateOf(false) }
            var showQuickAddSheet by remember { mutableStateOf(false) }
            var showGroundingModal by remember { mutableStateOf(false) }

            MeridianTheme(
                palette = state.palette,
                themeMode = state.themeMode
            ) {
                Scaffold(
                    topBar = {
                        MeridianTopAppBar(
                            currentRoute = state.currentRoute,
                            onOpenCommandPalette = { showCommandPalette = true },
                            onOpenThemeDialog = { showThemeDialog = true },
                            onOpenQuickAdd = { showQuickAddSheet = true }
                        )
                    },
                    bottomBar = {
                        MeridianBottomNavigationBar(
                            currentRoute = state.currentRoute,
                            onNavigate = { viewModel.navigateTo(it) },
                            journalBadge = if (state.journalEntries.isNotEmpty()) "${state.journalEntries.size}" else null,
                            recoveryBadge = if (state.recoveryQuits.isNotEmpty()) "7d" else null
                        )
                    },
                    contentWindowInsets = WindowInsets.safeDrawing,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (state.isLoading) {
                            OverviewSkeleton()
                        } else {
                            when (state.currentRoute) {
                                ModuleRoute.OVERVIEW -> OverviewScreen(
                                    state = state,
                                    onNavigate = { viewModel.navigateTo(it) },
                                    onUpdateWeights = { viewModel.updateWeights(it) }
                                )
                                ModuleRoute.TIMELINE -> TimelineScreen(
                                    timelineItems = state.timeline
                                )
                                ModuleRoute.JOURNAL -> JournalScreen(
                                    entries = state.journalEntries,
                                    onAddEntry = { text, tag, pinned -> viewModel.addJournalEntry(text, tag, pinned) },
                                    onTogglePin = { viewModel.toggleJournalPin(it) },
                                    onDeleteEntry = { viewModel.deleteJournal(it) }
                                )
                                ModuleRoute.STUDY -> StudyScreen(
                                    studyLogs = state.studyLogs,
                                    completedTopicIds = state.completedTopicIds,
                                    onLogSession = { dur, subj, top, note, focus ->
                                        viewModel.logStudySession(dur, subj, top, note, focus)
                                    },
                                    onToggleTopicDone = { id, done -> viewModel.toggleTopicDone(id, done) }
                                )
                                ModuleRoute.RECOVERY -> RecoveryScreen(
                                    quits = state.recoveryQuits,
                                    logs = state.recoveryLogs,
                                    onLogUrge = { quitId, note -> viewModel.logUrge(quitId, note) },
                                    onLogReset = { quitId, reason -> viewModel.logReset(quitId, reason) },
                                    onOpenGrounding = { showGroundingModal = true }
                                )
                                ModuleRoute.FINANCE -> FinanceScreen(
                                    accounts = state.financeAccounts,
                                    categories = state.financeCategories,
                                    transactions = state.financeTransactions,
                                    onAddTransaction = { type, amt, merchant, cat, note ->
                                        viewModel.addTransaction(type, amt, merchant, cat, note)
                                    },
                                    onDeleteTransaction = { viewModel.deleteTransaction(it) }
                                )
                                ModuleRoute.PULSE -> PulseScreen(
                                    pulseLogs = state.pulseLogs,
                                    habits = state.pulseHabits,
                                    onLogPulse = { sleep, quality, mood, energy, focus, note ->
                                        viewModel.logPulse(sleep, quality, mood, energy, focus, note)
                                    }
                                )
                                ModuleRoute.GOALS -> GoalsScreen(
                                    goals = state.goals,
                                    checkins = state.goalCheckins,
                                    onAddGoal = { title, cat, target, unit, deadline ->
                                        viewModel.addGoal(title, cat, target, unit, deadline)
                                    },
                                    onLogCheckin = { id, inc, note -> viewModel.logGoalCheckin(id, inc, note) },
                                    onDeleteGoal = { viewModel.deleteGoal(it) }
                                )
                            }
                        }
                    }

                    // Modals & Bottom Sheets
                    if (showQuickAddSheet) {
                        QuickAddBottomSheet(
                            onDismiss = { showQuickAddSheet = false },
                            onNavigate = {
                                viewModel.navigateTo(it)
                                showQuickAddSheet = false
                            },
                            onQuickJournal = { text -> viewModel.addJournalEntry(text, null) },
                            onQuickStudy = { mins, topic ->
                                viewModel.logStudySession(mins, "Medical Sciences", topic, null)
                            },
                            onQuickUrge = {
                                val firstQuit = state.recoveryQuits.firstOrNull()
                                if (firstQuit != null) viewModel.logUrge(firstQuit.id, null)
                            },
                            onOpenGrounding = {
                                showGroundingModal = true
                            }
                        )
                    }

                    if (showGroundingModal) {
                        GroundingUrgeSurferModal(
                            onDismiss = { showGroundingModal = false },
                            onUrgeOvercome = {
                                val firstQuit = state.recoveryQuits.firstOrNull()
                                if (firstQuit != null) viewModel.logUrge(firstQuit.id, "Surfed urge via 5-4-3-2-1 Grounding protocol.")
                            }
                        )
                    }

                    if (showThemeDialog) {
                        ThemeSelectorDialog(
                            currentPalette = state.palette,
                            currentMode = state.themeMode,
                            onSelectPalette = { viewModel.setPalette(it) },
                            onSelectMode = { viewModel.setThemeMode(it) },
                            onDismiss = { showThemeDialog = false }
                        )
                    }

                    if (showCommandPalette) {
                        CommandPaletteDialog(
                            onNavigate = {
                                viewModel.navigateTo(it)
                                showCommandPalette = false
                            },
                            onDismiss = { showCommandPalette = false }
                        )
                    }
                }
            }
        }
    }
}
