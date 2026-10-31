package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.components.AiAssistantDialog
import com.example.ui.components.AppBottomNavigationBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.NotificationsDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExamsScreen
import com.example.ui.screens.GroupDetailScreen
import com.example.ui.screens.GroupsScreen
import com.example.ui.screens.PomodoroScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.SmartStudyTheme
import com.example.util.AppLanguage
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by mainViewModel.isDarkMode.collectAsState()
            val language by mainViewModel.appLanguage.collectAsState()
            val currentScreen by mainViewModel.currentScreen.collectAsState()
            val alerts by mainViewModel.alerts.collectAsState()
            val isAiLoading by mainViewModel.isAiLoading.collectAsState()
            val aiResponse by mainViewModel.aiResponse.collectAsState()

            var showNotificationsDialog by remember { mutableStateOf(false) }
            var showAiDialog by remember { mutableStateOf(false) }

            val layoutDirection = if (language == AppLanguage.ARABIC) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            // Handle system Back Press with proper backstack
            BackHandler(enabled = currentScreen != Screen.WELCOME) {
                mainViewModel.navigateBack()
            }

            SmartStudyTheme(darkTheme = isDarkMode) {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    if (currentScreen == Screen.WELCOME) {
                        WelcomeScreen(viewModel = mainViewModel)
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                AppTopBar(
                                    viewModel = mainViewModel,
                                    onOpenNotifications = { showNotificationsDialog = true },
                                    onOpenAiAssistant = { showAiDialog = true }
                                )
                            },
                            bottomBar = {
                                AppBottomNavigationBar(
                                    currentScreen = currentScreen,
                                    language = language,
                                    onNavigate = { mainViewModel.navigateTo(it) }
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(innerPadding)
                            ) {
                                when (currentScreen) {
                                    Screen.DASHBOARD -> DashboardScreen(
                                        viewModel = mainViewModel,
                                        onOpenAddTask = { mainViewModel.navigateTo(Screen.TASKS) }
                                    )
                                    Screen.TASKS -> TasksScreen(viewModel = mainViewModel)
                                    Screen.SUBJECTS -> SubjectsScreen(viewModel = mainViewModel)
                                    Screen.EXAMS -> ExamsScreen(viewModel = mainViewModel)
                                    Screen.SCHEDULE -> ScheduleScreen(viewModel = mainViewModel)
                                    Screen.GROUPS -> GroupsScreen(viewModel = mainViewModel)
                                    Screen.GROUP_DETAIL -> GroupDetailScreen(viewModel = mainViewModel)
                                    Screen.POMODORO -> PomodoroScreen(viewModel = mainViewModel)
                                    Screen.ANALYTICS -> AnalyticsScreen(viewModel = mainViewModel)
                                    Screen.WELCOME -> {}
                                }
                            }
                        }
                    }

                    // In-app Notifications Dialog
                    if (showNotificationsDialog) {
                        NotificationsDialog(
                            alerts = alerts,
                            language = language,
                            onDismiss = { showNotificationsDialog = false }
                        )
                    }

                    // Gemini AI Assistant Dialog
                    if (showAiDialog) {
                        AiAssistantDialog(
                            isLoading = isAiLoading,
                            response = aiResponse,
                            language = language,
                            onGenerateDailyPlan = { mainViewModel.generateDailyStudyPlan(language) },
                            onAskQuestion = { query -> mainViewModel.askAiAssistant(query, language) },
                            onDismiss = { showAiDialog = false }
                        )
                    }
                }
            }
        }
    }
}
