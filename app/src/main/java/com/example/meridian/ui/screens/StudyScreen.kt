package com.example.meridian.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.meridian.data.curriculum.MedicalCurriculum
import com.example.meridian.data.curriculum.StudyDayTopic
import com.example.meridian.data.model.StudyLogEntity

@Composable
fun StudyScreen(
    studyLogs: List<StudyLogEntity>,
    completedTopicIds: Set<String>,
    onLogSession: (Int, String?, String?, String?, Int) -> Unit,
    onToggleTopicDone: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubjectCode by remember { mutableStateOf("ANA") }
    var showLogSessionDialog by remember { mutableStateOf(false) }

    val currentSubject = remember(selectedSubjectCode) {
        MedicalCurriculum.subjects.first { it.code == selectedSubjectCode }
    }

    val totalHoursThisWeek = remember(studyLogs) {
        val totalMins = studyLogs.sumOf { it.durationMins }
        (totalMins / 60.0)
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showLogSessionDialog = true },
                icon = { Icon(Icons.Default.Timer, contentDescription = null) },
                text = { Text("Log Session") },
                containerColor = Color(0xFF2D6A4F),
                contentColor = Color.White,
                modifier = Modifier.testTag("log_study_fab")
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .testTag("study_screen")
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Study Weekly Banner Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "200L MEDICAL CURRICULUM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "%.1f Hours Logged".format(totalHoursThisWeek),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Target: 20 hrs/week • ${completedTopicIds.size} topics mastered",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2D6A4F).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color(0xFF2D6A4F),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Curriculum Subject Tabs (Anatomy, Physiology, Biochemistry)
            TabRow(
                selectedTabIndex = MedicalCurriculum.subjects.indexOfFirst { it.code == selectedSubjectCode },
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.clip(RoundedCornerShape(14.dp))
            ) {
                MedicalCurriculum.subjects.forEach { subj ->
                    val isSelected = selectedSubjectCode == subj.code
                    Tab(
                        selected = isSelected,
                        onClick = { selectedSubjectCode = subj.code },
                        text = {
                            Text(
                                text = subj.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Topic List for Selected Subject
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(currentSubject.topics, key = { it.id }) { topic ->
                    val isDone = completedTopicIds.contains(topic.id)
                    StudyTopicCard(
                        topic = topic,
                        isCompleted = isDone,
                        onToggleDone = { onToggleTopicDone(topic.id, isDone) }
                    )
                }
            }
        }
    }

    if (showLogSessionDialog) {
        LogStudySessionDialog(
            defaultSubject = currentSubject.name,
            onDismiss = { showLogSessionDialog = false },
            onSave = { duration, subject, topic, note, focus ->
                onLogSession(duration, subject, topic, note, focus)
                showLogSessionDialog = false
            }
        )
    }
}

@Composable
fun StudyTopicCard(
    topic: StudyDayTopic,
    isCompleted: Boolean,
    onToggleDone: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { onToggleDone() },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2D6A4F))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${topic.moduleCode} • DAY ${topic.dayNum}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2D6A4F),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${topic.targetMins}m target",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand details"
                    )
                }
            }

            Text(
                text = topic.brief,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 48.dp)
            )

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp, top = 12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "ANKI CLOZE PROMPT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = topic.anki,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ACTIVE RECALL QUESTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    topic.recallQuestions.forEachIndexed { i, q ->
                        Text(
                            text = "${i + 1}. $q",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LogStudySessionDialog(
    defaultSubject: String,
    onDismiss: () -> Unit,
    onSave: (Int, String, String, String, Int) -> Unit
) {
    var durationStr by remember { mutableStateOf("50") }
    var subject by remember { mutableStateOf(defaultSubject) }
    var topic by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var focusScore by remember { mutableStateOf(4) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Log Study Session", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = durationStr,
                    onValueChange = { durationStr = it },
                    label = { Text("Duration (Minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic Studied") },
                    placeholder = { Text("e.g. Femoral Triangle boundaries") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Active Recall Notes") },
                    placeholder = { Text("Key insights, memory hooks, Anki card count...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Focus Score: $focusScore / 5", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = focusScore.toFloat(),
                    onValueChange = { focusScore = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val duration = durationStr.toIntOrNull() ?: 45
                            onSave(duration, subject, topic, note, focusScore)
                        }
                    ) {
                        Text("Log Session")
                    }
                }
            }
        }
    }
}
