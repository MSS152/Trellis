package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.local.TaskEntity
import com.example.ui.theme.GamifiedCyan
import com.example.ui.theme.GamifiedElectricBlue
import com.example.ui.theme.GamifiedEmerald
import com.example.ui.theme.GamifiedFlameOrange
import com.example.ui.theme.GamifiedGold
import java.time.LocalDate

enum class CompletionWidgetMode(val label: String) {
    COMBINED("All Today"),
    TASKS("Tasks Only"),
    HABITS("Habits Only")
}

@Composable
fun TodayTaskCompletionWidget(
    tasks: List<TaskEntity>,
    habits: List<HabitEntity>,
    logs: List<HabitLogEntity>,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(CompletionWidgetMode.COMBINED) }

    val todayKey = remember { LocalDate.now().toString() }

    // Calculate Task Metrics
    val totalTasks = tasks.size.coerceAtLeast(1)
    val doneTasks = tasks.count { it.status.equals("Done", ignoreCase = true) }
    val taskRate = (doneTasks.toFloat() / totalTasks).coerceIn(0f, 1f)

    // Calculate Habit Metrics
    val totalHabits = habits.size.coerceAtLeast(1)
    val doneHabits = habits.count { h -> logs.any { it.habitId == h.id && it.dateString == todayKey && it.completed } }
    val habitRate = (doneHabits.toFloat() / totalHabits).coerceIn(0f, 1f)

    // Combined Metrics
    val totalItems = tasks.size + habits.size
    val totalDone = doneTasks + doneHabits
    val combinedRate = if (totalItems > 0) (totalDone.toFloat() / totalItems).coerceIn(0f, 1f) else 0f

    val currentRate = when (mode) {
        CompletionWidgetMode.COMBINED -> combinedRate
        CompletionWidgetMode.TASKS -> taskRate
        CompletionWidgetMode.HABITS -> habitRate
    }

    val animatedRate by animateFloatAsState(
        targetValue = currentRate,
        animationSpec = tween(durationMillis = 800),
        label = "circular_progress"
    )

    val percentText = (animatedRate * 100).toInt()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(GamifiedElectricBlue.copy(alpha = 0.5f), GamifiedCyan.copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("today_completion_widget_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TODAY'S PROGRESS WIDGET",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Task & Habit Completion Ring",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            when {
                                percentText >= 100 -> GamifiedEmerald.copy(alpha = 0.25f)
                                percentText >= 50 -> GamifiedCyan.copy(alpha = 0.2f)
                                else -> GamifiedFlameOrange.copy(alpha = 0.2f)
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            when {
                                percentText >= 100 -> GamifiedEmerald
                                percentText >= 50 -> GamifiedCyan
                                else -> GamifiedFlameOrange
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            percentText >= 100 -> "🎉 100% Done!"
                            percentText >= 50 -> "🔥 On Track"
                            else -> "⚡ Keep Going"
                        },
                        color = when {
                            percentText >= 100 -> GamifiedEmerald
                            percentText >= 50 -> GamifiedCyan
                            else -> GamifiedFlameOrange
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mode Toggle Selector: [All Today] • [Tasks Only] • [Habits Only]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CompletionWidgetMode.values().forEach { widgetMode ->
                    val isSelected = mode == widgetMode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) GamifiedElectricBlue else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) GamifiedElectricBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { mode = widgetMode }
                            .padding(vertical = 6.dp)
                            .testTag("widget_mode_${widgetMode.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = widgetMode.label,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Central Ring Section + Metric Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Circular Progress Canvas Ring
                Box(
                    modifier = Modifier
                        .size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    val activeBrush = Brush.sweepGradient(
                        listOf(GamifiedElectricBlue, GamifiedCyan, GamifiedEmerald, GamifiedElectricBlue)
                    )

                    Canvas(modifier = Modifier.size(120.dp)) {
                        val strokeWidth = 12.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = Size(diameter, diameter)

                        // Draw Background Ring Track
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Draw Dynamic Animated Progress Arc
                        val sweepAngle = animatedRate * 360f
                        if (sweepAngle > 0f) {
                            drawArc(
                                brush = activeBrush,
                                startAngle = -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$percentText%",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = when (mode) {
                                CompletionWidgetMode.COMBINED -> "$totalDone/$totalItems Done"
                                CompletionWidgetMode.TASKS -> "$doneTasks/$totalTasks Tasks"
                                CompletionWidgetMode.HABITS -> "$doneHabits/$totalHabits Habits"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Side Metric Breakdown Bars
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Task Completion Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📋 Tasks", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Text(
                                    text = "$doneTasks/$totalTasks Done",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GamifiedElectricBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { taskRate },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = GamifiedElectricBlue,
                                trackColor = MaterialTheme.colorScheme.background
                            )
                        }
                    }

                    // Habit Completion Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⚡ Habits", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Text(
                                    text = "$doneHabits/$totalHabits Done",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GamifiedEmerald
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { habitRate },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = GamifiedEmerald,
                                trackColor = MaterialTheme.colorScheme.background
                            )
                        }
                    }
                }
            }
        }
    }
}
