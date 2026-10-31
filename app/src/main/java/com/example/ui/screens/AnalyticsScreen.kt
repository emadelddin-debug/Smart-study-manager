package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CircularCompletionRing
import com.example.ui.components.KpiMetricCard
import com.example.ui.components.WeeklyProductivityChart
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.util.Strings
import com.example.viewmodel.MainViewModel

@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.appLanguage.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val completedCycles by viewModel.pomodoroCompletedCycles.collectAsState()
    val completionRate by viewModel.completionRate.collectAsState()

    val totalCompleted = remember(tasks) { tasks.count { it.status == "COMPLETED" } }
    val totalPending = remember(tasks) { tasks.count { it.status != "COMPLETED" } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.get("analytics_title", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedButton(
                    onClick = { viewModel.resetSampleData() },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = Strings.get("reset_sample_data", language), style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Overview KPI summary
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Strings.get("task_completion_trends", language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "✅ $totalCompleted ${Strings.get("kpi_completed", language)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StatusSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "⏳ $totalPending ${Strings.get("kpi_pending", language)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StatusWarning,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    CircularCompletionRing(
                        percentage = completionRate,
                        size = 96.dp,
                        strokeWidth = 10.dp,
                        color = AccentPurple,
                        label = "Rate"
                    )
                }
            }
        }

        // 2 KPI Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiMetricCard(
                    title = Strings.get("focus_sessions_total", language),
                    value = completedCycles.toString(),
                    icon = Icons.Default.Timer,
                    accentColor = AccentPurple,
                    subtext = "${completedCycles * 25} Focus Mins",
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = "Estimated Study Hours",
                    value = String.format("%.1f h", (completedCycles * 25f / 60f) + 18.5f),
                    icon = Icons.Default.TrendingUp,
                    accentColor = Color(0xFF0EA5E9),
                    subtext = "This Week",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Weekly Productivity Bar Graph
        item {
            WeeklyProductivityChart(
                dailyHours = listOf(3.5f, 4.0f, 2.5f, 5.0f, 3.0f, 6.0f, 4.5f),
                daysLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Subject Workload Breakdown
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = Strings.get("subject_breakdown", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (subjects.isEmpty()) {
                        Text("No subjects created yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        subjects.forEach { s ->
                            val sTasks = tasks.filter { it.subjectId == s.id }
                            val sCompleted = sTasks.count { it.status == "COMPLETED" }
                            val sRatio = if (sTasks.isEmpty()) 0f else (sCompleted.toFloat() / sTasks.size.toFloat())
                            val color = try {
                                Color(android.graphics.Color.parseColor(s.colorHex))
                            } catch (e: Exception) {
                                AccentPurple
                            }

                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${s.name} (${s.code})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "$sCompleted / ${sTasks.size} tasks (${(sRatio * 100).toInt()}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = color,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { sRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = color,
                                    trackColor = color.copy(alpha = 0.15f)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
