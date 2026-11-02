package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.ui.theme.GamifiedCyan
import com.example.ui.theme.GamifiedElectricBlue
import com.example.ui.theme.GamifiedEmerald
import com.example.ui.theme.GamifiedFlameOrange

@Composable
fun HabitLoggingModal(
    habit: HabitEntity,
    onDismiss: () -> Unit,
    onConfirm: (numericValue: Double, intensityRating: Int, notes: String) -> Unit
) {
    var numericInput by remember { mutableStateOf(habit.targetUnitValue.toInt().toString()) }
    var intensityRating by remember { mutableFloatStateOf(5f) }
    var notes by remember { mutableStateOf("") }

    val isNegative = habit.habitType.equals("NEGATIVE", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(habit.emoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(
                    text = if (isNegative) "Log Clean Day: ${habit.title}" else "Log Progress: ${habit.title}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (isNegative) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GamifiedEmerald.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, GamifiedEmerald, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("🛡️ Breaking Bad Habit", color = GamifiedEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Logging today as clean! Clean Days protect your streak shield and earn bonus XP.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else if (habit.unitType != "COMPLETION") {
                    OutlinedTextField(
                        value = numericInput,
                        onValueChange = { numericInput = it },
                        label = { Text("Value (${habit.unitLabel}) — Goal: ${habit.targetUnitValue}") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = GamifiedElectricBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("numeric_unit_input")
                    )
                }

                // Effort Intensity Scale Slider (1 to 10)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Effort Intensity", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = "${intensityRating.toInt()} / 10 ${getIntensityEmoji(intensityRating.toInt())}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GamifiedCyan
                        )
                    }
                    Slider(
                        value = intensityRating,
                        onValueChange = { intensityRating = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = GamifiedCyan,
                            activeTrackColor = GamifiedCyan,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("intensity_slider")
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Reflection Notes (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = GamifiedElectricBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedVal = numericInput.toDoubleOrNull() ?: habit.targetUnitValue
                    onConfirm(parsedVal, intensityRating.toInt(), notes)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isNegative) GamifiedEmerald else GamifiedElectricBlue
                ),
                modifier = Modifier.testTag("confirm_habit_log_button")
            ) {
                Text(if (isNegative) "Log Clean Day (+50 XP)" else "Log Progress", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

fun getIntensityEmoji(rating: Int): String {
    return when {
        rating <= 2 -> "😴 Light"
        rating <= 5 -> "⚡ Moderate"
        rating <= 8 -> "🔥 High"
        else -> "🚀 Maximum Effort"
    }
}
