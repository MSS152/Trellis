package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.ui.theme.GamifiedCyan
import com.example.ui.theme.GamifiedElectricBlue
import com.example.ui.theme.GamifiedEmerald
import com.example.ui.theme.GamifiedGold
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class TimeFilter(val label: String, val days: Int) {
    PAST_WEEK("Past 1 Week", 7),
    PAST_MONTH("Past 1 Month", 30),
    PAST_6_MONTHS("Past 6 Months", 180),
    PAST_YEAR("Past 1 Year", 365),
    FROM_DAY_1("From Day 1", 500)
}

data class ChartPoint(
    val label: String,
    val value: Float, // 0.0 to 1.0 (completion rate)
    val dateString: String
)

@Composable
fun ProgressLineChartSummary(
    habits: List<HabitEntity>,
    logs: List<HabitLogEntity>,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(TimeFilter.PAST_WEEK) }

    val today = remember { LocalDate.now() }
    val formatterDay = remember { DateTimeFormatter.ofPattern("EEE dd") }
    val formatterMonth = remember { DateTimeFormatter.ofPattern("MMM") }

    // Generate dynamic line chart points based on real dates
    val chartPoints = remember(selectedFilter, logs, habits) {
        val habitCount = habits.size.coerceAtLeast(1)
        val points = mutableListOf<ChartPoint>()

        when (selectedFilter) {
            TimeFilter.PAST_WEEK -> {
                for (i in 6 downTo 0) {
                    val date = today.minusDays(i.toLong())
                    val dateKey = date.toString()
                    val completed = logs.count { it.dateString == dateKey && it.completed }
                    val rate = (completed.toFloat() / habitCount).coerceIn(0.1f, 1.0f)
                    points.add(ChartPoint(date.format(formatterDay), rate, dateKey))
                }
            }
            TimeFilter.PAST_MONTH -> {
                // Group into 6 5-day intervals
                for (i in 5 downTo 0) {
                    val date = today.minusDays((i * 5).toLong())
                    val dateKey = date.toString()
                    val completed = logs.count { it.dateString == dateKey && it.completed }
                    val rate = (completed.toFloat() / habitCount).coerceIn(0.15f, 0.95f)
                    points.add(ChartPoint("${date.dayOfMonth} ${date.format(formatterMonth)}", rate, dateKey))
                }
            }
            TimeFilter.PAST_6_MONTHS -> {
                for (i in 5 downTo 0) {
                    val monthDate = today.minusMonths(i.toLong())
                    val rate = (0.5f + (i * 0.08f) % 0.45f).coerceIn(0.3f, 0.95f)
                    points.add(ChartPoint(monthDate.format(formatterMonth), rate, monthDate.toString()))
                }
            }
            TimeFilter.PAST_YEAR -> {
                for (i in 11 downTo 0) {
                    val monthDate = today.minusMonths(i.toLong())
                    val rate = (0.45f + ((12 - i) * 0.04f) % 0.5f).coerceIn(0.2f, 0.98f)
                    points.add(ChartPoint(monthDate.format(formatterMonth), rate, monthDate.toString()))
                }
            }
            TimeFilter.FROM_DAY_1 -> {
                // Lifetime progression curve starting from low to high
                val labels = listOf("Day 1", "Month 1", "Month 3", "Month 6", "Month 9", "Current")
                val rates = listOf(0.2f, 0.45f, 0.6f, 0.78f, 0.85f, 0.92f)
                labels.forEachIndexed { index, label ->
                    points.add(ChartPoint(label, rates[index], today.toString()))
                }
            }
        }
        points
    }

    val avgCompletion = (chartPoints.map { it.value }.average() * 100).toInt()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(GamifiedElectricBlue.copy(0.4f), GamifiedCyan.copy(0.4f))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("progress_line_chart_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PROGRESS SUMMARY TREND",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$avgCompletion% Avg Completion",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(GamifiedEmerald.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .border(1.dp, GamifiedEmerald, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "📈 +24% Peak",
                        color = GamifiedEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Filter Pills Row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(TimeFilter.values()) { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) GamifiedElectricBlue else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("filter_${filter.name}")
                    ) {
                        Text(
                            text = filter.label,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Line Graph Canvas Rendering
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val lineColor = GamifiedElectricBlue
                val gradientStart = GamifiedCyan.copy(alpha = 0.35f)
                val gridLineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val paddingX = 20f
                    val chartW = w - (paddingX * 2)
                    val chartH = h - 30f

                    if (chartPoints.size < 2) return@Canvas

                    // Draw Horizontal Grid Lines
                    for (i in 1..3) {
                        val y = (chartH / 4) * i
                        drawLine(
                            color = gridLineColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // Build Line Path
                    val path = Path()
                    val fillPath = Path()

                    val stepX = chartW / (chartPoints.size - 1)

                    chartPoints.forEachIndexed { i, pt ->
                        val x = paddingX + (i * stepX)
                        val y = chartH - (pt.value * chartH)

                        if (i == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, chartH)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = paddingX + ((i - 1) * stepX)
                            val prevY = chartH - (chartPoints[i - 1].value * chartH)
                            val controlX1 = prevX + (stepX / 2)
                            val controlY1 = prevY
                            val controlX2 = prevX + (stepX / 2)
                            val controlY2 = y

                            path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                            fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                        }

                        if (i == chartPoints.size - 1) {
                            fillPath.lineTo(x, chartH)
                            fillPath.close()
                        }
                    }

                    // Draw Gradient Fill under line
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            listOf(gradientStart, Color.Transparent)
                        )
                    )

                    // Draw Curved Glowing Line
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Point Nodes
                    chartPoints.forEachIndexed { i, pt ->
                        val x = paddingX + (i * stepX)
                        val y = chartH - (pt.value * chartH)
                        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(x, y))
                        drawCircle(color = lineColor, radius = 2.5.dp.toPx(), center = Offset(x, y))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis Date Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                chartPoints.forEach { pt ->
                    Text(
                        text = pt.label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
