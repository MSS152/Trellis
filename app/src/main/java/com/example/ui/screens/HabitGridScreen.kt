package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MobileHabitGrid
import com.example.ui.theme.GamifiedCyan
import com.example.ui.theme.GamifiedElectricBlue
import com.example.ui.theme.GamifiedEmerald
import com.example.ui.theme.GamifiedFlameOrange
import com.example.ui.viewmodel.HabitFlowViewModel

@Composable
fun HabitGridScreen(
    viewModel: HabitFlowViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habits.collectAsState()
    val logs by viewModel.habitLogs.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GamifiedElectricBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_habit_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Habit Check",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Track positive habits, break bad habits, and log variable metrics",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            item {
                MobileHabitGrid(
                    habits = habits,
                    logs = logs,
                    onToggleDay = { habitId, dateString, currentState ->
                        viewModel.toggleHabit(habitId, dateString, !currentState)
                    },
                    onToggleWithDetails = { habitId, dateString, isDone, value, intensity, notes ->
                        viewModel.toggleHabitWithDetails(habitId, dateString, isDone, value, intensity, notes)
                    },
                    onAddHabit = { title, emoji, category, habitType ->
                        viewModel.addHabit(
                            title = title,
                            emoji = emoji,
                            category = category,
                            goalCount = 30,
                            habitType = habitType
                        )
                    },
                    onUpdateHabit = { habit ->
                        viewModel.updateHabit(habit)
                    },
                    onDeleteHabit = { habitId ->
                        viewModel.deleteHabit(habitId)
                    }
                )
            }
        }

        if (showAddDialog) {
            AddHabitDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, emoji, category, goal, habitType, frequency, weeklyTarget, unitType, targetValue, isHealthSynced ->
                    viewModel.addHabit(
                        title = title,
                        emoji = emoji,
                        category = category,
                        goalCount = goal,
                        habitType = habitType,
                        targetFrequency = frequency,
                        weeklyTargetCount = weeklyTarget,
                        unitType = unitType,
                        targetUnitValue = targetValue,
                        isHealthConnectSynced = isHealthSynced
                    )
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        emoji: String,
        category: String,
        goal: Int,
        habitType: String,
        frequency: String,
        weeklyTarget: Int,
        unitType: String,
        targetValue: Double,
        isHealthSynced: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("⚡") }
    var category by remember { mutableStateOf("Body") }
    var goalText by remember { mutableStateOf("30") }
    var habitType by remember { mutableStateOf("POSITIVE") } // POSITIVE vs NEGATIVE
    var frequency by remember { mutableStateOf("DAILY") } // DAILY vs WEEKLY_X_TIMES
    var weeklyTargetCount by remember { mutableStateOf(3) }
    var unitType by remember { mutableStateOf("COMPLETION") } // COMPLETION, STEPS, KM, MINUTES, PAGES, INTENSITY_SCALE
    var targetValueText by remember { mutableStateOf("10000") }
    var isHealthSynced by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = if (habitType == "NEGATIVE") "🚫 Break Bad Habit" else "🌱 Create Good Habit",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Habit Type Toggle: Build Good vs Break Bad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (habitType == "POSITIVE") GamifiedEmerald.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (habitType == "POSITIVE") GamifiedEmerald else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable { habitType = "POSITIVE"; emoji = "🌱" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌱 Build Good", color = if (habitType == "POSITIVE") GamifiedEmerald else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (habitType == "NEGATIVE") GamifiedFlameOrange.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (habitType == "NEGATIVE") GamifiedFlameOrange else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable { habitType = "NEGATIVE"; emoji = "🚫" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚫 Break Bad", color = if (habitType == "NEGATIVE") GamifiedFlameOrange else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (habitType == "NEGATIVE") "Habit to Break (e.g. Doomscrolling)" else "Habit Title (e.g. Drink 3L Water)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = GamifiedElectricBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("habit_title_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it },
                        label = { Text("Emoji") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = GamifiedElectricBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = goalText,
                        onValueChange = { goalText = it },
                        label = { Text("Goal Days") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = GamifiedElectricBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Frequency Toggle: Daily vs Flexible Weekly (e.g., 3x / week)
                Column {
                    Text("Frequency", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "DAILY" to "Every Day",
                            "WEEKLY_X_TIMES" to "3x / Week"
                        ).forEach { (freqKey, label) ->
                            val isSelected = frequency == freqKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) GamifiedElectricBlue else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { frequency = freqKey }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Metric Units Selector
                if (habitType == "POSITIVE") {
                    Column {
                        Text("Log Unit", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                "COMPLETION" to "Check",
                                "STEPS" to "Steps",
                                "KM" to "km",
                                "MINUTES" to "Mins",
                                "INTENSITY_SCALE" to "1-10 Scale"
                            ).forEach { (uKey, uLabel) ->
                                val isSelected = unitType == uKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) GamifiedCyan else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            unitType = uKey
                                        }
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(uLabel, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title,
                            emoji.ifBlank { if (habitType == "NEGATIVE") "🚫" else "🌱" },
                            category,
                            goalText.toIntOrNull() ?: 30,
                            habitType,
                            frequency,
                            if (frequency == "WEEKLY_X_TIMES") 3 else 7,
                            unitType,
                            targetValueText.toDoubleOrNull() ?: 1.0,
                            isHealthSynced
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (habitType == "NEGATIVE") GamifiedFlameOrange else GamifiedElectricBlue
                ),
                modifier = Modifier.testTag("save_habit_button")
            ) {
                Text("Save Habit", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
