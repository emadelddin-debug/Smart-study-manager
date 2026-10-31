package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.util.AppLanguage
import com.example.util.Strings
import com.example.viewmodel.MainViewModel

@Composable
fun ScheduleScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.appLanguage.collectAsState()
    val sessions by viewModel.studySessions.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    var selectedDayIndex by remember { mutableIntStateOf(1) } // 1 (Mon) - 7 (Sun)
    var isWeeklyView by remember { mutableStateOf(false) }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var sessionToEdit by remember { mutableStateOf<StudySessionEntity?>(null) }
    var sessionToDelete by remember { mutableStateOf<StudySessionEntity?>(null) }

    val daysOfWeek = remember {
        listOf(
            1 to "Mon",
            2 to "Tue",
            3 to "Wed",
            4 to "Thu",
            5 to "Fri",
            6 to "Sat",
            7 to "Sun"
        )
    }

    val subjectMap = remember(subjects) { subjects.associate { it.id to it } }

    val filteredSessions = remember(sessions, selectedDayIndex, isWeeklyView) {
        if (isWeeklyView) {
            sessions.sortedWith(compareBy({ it.dayOfWeek }, { it.startTime }))
        } else {
            sessions.filter { it.dayOfWeek == selectedDayIndex }.sortedBy { it.startTime }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header & View Toggle (Daily vs Weekly)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.get("schedule_title", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (!isWeeklyView) AccentPurple else Color.Transparent)
                            .clickable { isWeeklyView = false }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = Strings.get("daily_view", language),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (!isWeeklyView) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isWeeklyView) AccentPurple else Color.Transparent)
                            .clickable { isWeeklyView = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = Strings.get("weekly_view", language),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isWeeklyView) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Days of the week tabs (if Daily view)
            if (!isWeeklyView) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    daysOfWeek.forEach { (index, dayName) ->
                        val isSelected = selectedDayIndex == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) AccentPurple else MaterialTheme.colorScheme.surface)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) AccentPurple else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedDayIndex = index }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sessions List
            if (filteredSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No study sessions scheduled for this period.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSessions, key = { it.id }) { session ->
                        val subject = subjectMap[session.subjectId]
                        val dayLabel = daysOfWeek.find { it.first == session.dayOfWeek }?.second ?: "Day"

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("session_card_${session.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = session.isCompleted,
                                    onCheckedChange = { viewModel.toggleSessionCompleted(session) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = StatusSuccess,
                                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = session.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (session.isCompleted)
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isWeeklyView) {
                                            Text(
                                                text = "[$dayLabel] ",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentPurple
                                            )
                                        }
                                        if (subject != null) {
                                            Text(
                                                text = "${subject.code} • ",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AccentPurple
                                            )
                                        }
                                        Text(
                                            text = "${session.startTime} - ${session.endTime}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row {
                                    IconButton(onClick = {
                                        sessionToEdit = session
                                        showAddEditDialog = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(onClick = { sessionToDelete = session }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = StatusDanger,
                                            modifier = Modifier.size(18.dp)
                                        )
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
        }

        // Add Session FAB
        FloatingActionButton(
            onClick = {
                sessionToEdit = null
                showAddEditDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_session_fab"),
            containerColor = AccentPurple,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = Strings.get("add_session", language)
            )
        }
    }

    // Add / Edit Session Dialog with Validation
    if (showAddEditDialog) {
        AddEditSessionDialog(
            session = sessionToEdit,
            subjects = subjects,
            currentDayIndex = selectedDayIndex,
            language = language,
            onDismiss = {
                showAddEditDialog = false
                sessionToEdit = null
            },
            onSave = { subjectId, title, dayOfWeek, startTime, endTime ->
                if (sessionToEdit == null) {
                    val success = viewModel.addStudySession(subjectId, title, dayOfWeek, startTime, endTime)
                    if (success) {
                        showAddEditDialog = false
                        sessionToEdit = null
                    }
                } else {
                    viewModel.updateStudySession(
                        sessionToEdit!!.copy(
                            subjectId = subjectId,
                            title = title,
                            dayOfWeek = dayOfWeek,
                            startTime = startTime,
                            endTime = endTime
                        )
                    )
                    showAddEditDialog = false
                    sessionToEdit = null
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text(Strings.get("delete", language)) },
            text = { Text("Are you sure you want to remove ${sessionToDelete!!.title}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudySession(sessionToDelete!!)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
                ) {
                    Text(Strings.get("delete", language), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text(Strings.get("cancel", language))
                }
            }
        )
    }
}

@Composable
private fun AddEditSessionDialog(
    session: StudySessionEntity?,
    subjects: List<SubjectEntity>,
    currentDayIndex: Int,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (Long, String, Int, String, String) -> Unit
) {
    var title by remember { mutableStateOf(session?.title ?: "") }
    var selectedSubjectId by remember { mutableStateOf(session?.subjectId ?: (subjects.firstOrNull()?.id ?: 0L)) }
    var selectedDay by remember { mutableIntStateOf(session?.dayOfWeek ?: currentDayIndex) }
    var startTime by remember { mutableStateOf(session?.startTime ?: "10:00") }
    var endTime by remember { mutableStateOf(session?.endTime ?: "12:00") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val daysOfWeek = listOf(
        1 to "Mon",
        2 to "Tue",
        3 to "Wed",
        4 to "Thu",
        5 to "Fri",
        6 to "Sat",
        7 to "Sun"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (session == null) Strings.get("add_session", language) else Strings.get("edit_task", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        validationError = null
                    },
                    label = { Text(Strings.get("session_title", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subject Selector
                Text(
                    text = Strings.get("subject", language),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.forEach { s ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedSubjectId == s.id) AccentPurple else MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clickable { selectedSubjectId = s.id }
                        ) {
                            Text(
                                text = s.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedSubjectId == s.id) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Day of Week
                Text(
                    text = Strings.get("day_of_week", language),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    daysOfWeek.forEach { (index, dayName) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedDay == index) AccentPurple else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedDay = index }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedDay == index) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Start & End Times
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = {
                            startTime = it
                            validationError = null
                        },
                        label = { Text(Strings.get("start_time", language)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = {
                            endTime = it
                            validationError = null
                        },
                        label = { Text(Strings.get("end_time", language)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Validation error display
                if (validationError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = validationError!!,
                        color = StatusDanger,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(Strings.get("cancel", language))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                validationError = "Session focus / goal is required."
                            } else if (startTime >= endTime) {
                                validationError = Strings.get("time_validation_error", language)
                            } else {
                                onSave(selectedSubjectId, title, selectedDay, startTime, endTime)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Text(Strings.get("save", language), color = Color.White)
                    }
                }
            }
        }
    }
}
