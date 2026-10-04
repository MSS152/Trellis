package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.local.TaskEntity
import com.example.ui.theme.GamifiedCyan
import com.example.ui.theme.GamifiedElectricBlue
import com.example.ui.theme.GamifiedEmerald
import java.time.LocalDate

@Composable
fun TrellisBrandedProgressLogo(
    tasks: List<TaskEntity>,
    habits: List<HabitEntity>,
    logs: List<HabitLogEntity>,
    modifier: Modifier = Modifier,
    logoSize: Dp = 68.dp
) {
    val todayKey = remember { LocalDate.now().toString() }

    // Task Completion
    val totalTasks = tasks.size.coerceAtLeast(1)
    val doneTasks = tasks.count { it.status.equals("Done", ignoreCase = true) }

    // Habit Completion
    val totalHabits = habits.size.coerceAtLeast(1)
    val doneHabits = habits.count { h -> logs.any { it.habitId == h.id && it.dateString == todayKey && it.completed } }

    // Combined Rate
    val totalItems = tasks.size + habits.size
    val totalDone = doneTasks + doneHabits
    val completionRate = if (totalItems > 0) (totalDone.toFloat() / totalItems).coerceIn(0f, 1f) else 0f

    val animatedRate by animateFloatAsState(
        targetValue = completionRate,
        animationSpec = tween(durationMillis = 1000),
        label = "trellis_logo_progress"
    )

    val percentText = (animatedRate * 100).toInt()

    Row(
        modifier = modifier.testTag("trellis_branded_logo_row"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Trellis Tree Shield with Live Dynamic Circular Progress Ring
        Box(
            modifier = Modifier.size(logoSize),
            contentAlignment = Alignment.Center
        ) {
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            val activeBrush = Brush.sweepGradient(
                listOf(GamifiedElectricBlue, GamifiedCyan, GamifiedEmerald, GamifiedElectricBlue)
            )

            Canvas(modifier = Modifier.size(logoSize)) {
                val strokeWidth = 6.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                val arcSize = Size(diameter, diameter)

                // Background Track Ring
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Dynamic Live Progress Ring around Logo
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

            // Inner Badge displaying Trellis Tree of Growth Vector Emblem
            Box(
                modifier = Modifier
                    .size(logoSize - 14.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A))
                    .border(1.dp, GamifiedElectricBlue.copy(alpha = 0.6f), CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_trellis_logo_vector),
                    contentDescription = "Trellis Logo",
                    modifier = Modifier.clip(CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "TRELLIS",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(GamifiedEmerald.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .border(1.dp, GamifiedEmerald, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$percentText%",
                        color = GamifiedEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Text(
                text = "Today: $totalDone/$totalItems items completed",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
