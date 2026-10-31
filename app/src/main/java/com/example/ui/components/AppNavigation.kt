package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.StatusDanger
import com.example.util.AppLanguage
import com.example.util.Strings
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    viewModel: MainViewModel,
    onOpenNotifications: () -> Unit,
    onOpenAiAssistant: () -> Unit
) {
    val language by viewModel.appLanguage.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val alerts by viewModel.alerts.collectAsState()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Identity Brand Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { viewModel.navigateTo(Screen.DASHBOARD) }
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_icon_1793454162828),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = Strings.get("app_name", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Quick Actions: AI Assistant, Alerts, Language, Theme
            Row(verticalAlignment = Alignment.CenterVertically) {
                // AI Assistant Button
                IconButton(
                    onClick = onOpenAiAssistant,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AccentPurple.copy(alpha = 0.15f))
                        .testTag("top_bar_ai_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Notifications Bell with Badge
                IconButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier.size(38.dp)
                ) {
                    BadgedBox(
                        badge = {
                            if (alerts.isNotEmpty()) {
                                Badge(
                                    containerColor = StatusDanger,
                                    contentColor = Color.White
                                ) {
                                    Text(alerts.size.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Language Switcher
                OutlinedButton(
                    onClick = {
                        val next = if (language == AppLanguage.ENGLISH) AppLanguage.ARABIC else AppLanguage.ENGLISH
                        viewModel.setLanguage(next)
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("language_switcher_button")
                ) {
                    Text(
                        text = if (language == AppLanguage.ENGLISH) "عربي" else "EN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Dark Mode Toggle
                IconButton(
                    onClick = { viewModel.toggleDarkMode() },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    currentScreen: Screen,
    language: AppLanguage,
    onNavigate: (Screen) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        val navItems = listOf(
            Triple(Screen.DASHBOARD, Strings.get("nav_dashboard", language), Icons.Default.Dashboard),
            Triple(Screen.TASKS, Strings.get("nav_tasks", language), Icons.Default.Assignment),
            Triple(Screen.EXAMS, Strings.get("nav_exams", language), Icons.Default.School),
            Triple(Screen.SCHEDULE, Strings.get("nav_schedule", language), Icons.Default.CalendarToday),
            Triple(Screen.GROUPS, Strings.get("nav_groups", language), Icons.Default.Group),
            Triple(Screen.POMODORO, Strings.get("nav_pomodoro", language), Icons.Default.Timer),
            Triple(Screen.SUBJECTS, Strings.get("nav_subjects", language), Icons.Default.Book),
            Triple(Screen.ANALYTICS, Strings.get("nav_analytics", language), Icons.Default.BarChart)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { (screen, label, icon) ->
                val isSelected = currentScreen == screen
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) AccentPurple.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { onNavigate(screen) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("nav_${screen.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) AccentPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentPurple
                            )
                        }
                    }
                }
            }
        }
    }
}
