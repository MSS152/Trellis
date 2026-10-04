package com.example.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.ui.theme.GamifiedCyan
import com.example.ui.theme.GamifiedElectricBlue
import com.example.ui.theme.GamifiedEmerald
import com.example.ui.theme.GamifiedFlameOrange
import com.example.ui.theme.GamifiedGold
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

enum class HabitCheckViewMode(val label: String) {
    TODAY("Today"),
    WEEKLY("7-Day"),
    MONTHLY("Month")
}

@Composable
fun HabitPillarBadge(category: String, modifier: Modifier = Modifier) {
    val (label, emoji, color) = when (category.lowercase()) {
        "food", "nutrition" -> Triple("Food", "🥑", GamifiedEmerald)
        "body", "fitness", "health" -> Triple("Body", "🏋️", GamifiedElectricBlue)
        "mind", "mindset", "growth" -> Triple("Mind", "🧠", Color(0xFF8B5CF6))
        else -> Triple(category.ifBlank { "Mind" }, "✨", GamifiedCyan)
    }

    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileHabitGrid(
    habits: List<HabitEntity>,
    logs: List<HabitLogEntity>,
    onToggleDay: (habitId: Long, dateString: String, currentState: Boolean) -> Unit,
    onUpdateHabit: (HabitEntity) -> Unit = {},
    onDeleteHabit: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(HabitCheckViewMode.TODAY) }
    var weekOffset by remember { mutableIntStateOf(0) }
    var monthOffset by remember { mutableIntStateOf(0) }

    var editingHabit by remember { mutableStateOf<HabitEntity?>(null) }
    var deletingHabit by remember { mutableStateOf<HabitEntity?>(null) }
    var selectedSummaryDate by remember { mutableStateOf<LocalDate?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val today = remember { LocalDate.now() }
    val formatterTodayFull = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy") }
    val formatterMonthYear = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }
    val formatterDayName = remember { DateTimeFormatter.ofPattern("EEE") }
    val formatterDayNum = remember { DateTimeFormatter.ofPattern("dd") }
    val formatterRange = remember { DateTimeFormatter.ofPattern("MMM dd") }

    val currentWeekDays = remember(weekOffset) {
        val baseDate = today.plusWeeks(weekOffset.toLong())
        val monday = baseDate.minusDays((baseDate.dayOfWeek.value - 1).toLong())
        (0..6).map { monday.plusDays(it.toLong()) }
    }

    val currentMonth = remember(monthOffset) {
        YearMonth.from(today.plusMonths(monthOffset.toLong()))
    }

    val monthYearText = currentWeekDays.first().format(formatterMonthYear)
    val weekRangeText = "${currentWeekDays.first().format(formatterRange)} - ${currentWeekDays.last().format(formatterRange)}"

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 3-Pill Toggle Row: [Today] • [7-Day] • [Month]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HabitCheckViewMode.values().forEach { mode ->
                val isSelected = viewMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) GamifiedElectricBlue else MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) GamifiedElectricBlue else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { viewMode = mode }
                        .padding(vertical = 10.dp)
                        .testTag("view_mode_${mode.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        when (viewMode) {
            HabitCheckViewMode.TODAY -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = today.format(formatterTodayFull).uppercase(),
                            color = GamifiedCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val todayStr = today.toString()
                        val doneCount = habits.count { h -> logs.any { it.habitId == h.id && it.dateString == todayStr && it.completed } }

                        Text(
                            text = "Today's Checklist ($doneCount/${habits.size} Done)",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "💡 Slide Right to Edit • Slide Left to Delete",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        habits.forEach { habit ->
                            val isDone = logs.any { it.habitId == habit.id && it.dateString == todayStr && it.completed }

                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    when (dismissValue) {
                                        SwipeToDismissBoxValue.StartToEnd -> {
                                            editingHabit = habit
                                            false
                                        }
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            deletingHabit = habit
                                            false
                                        }
                                        else -> false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val direction = dismissState.dismissDirection
                                    val bgColor = when (direction) {
                                        SwipeToDismissBoxValue.StartToEnd -> GamifiedElectricBlue
                                        SwipeToDismissBoxValue.EndToStart -> Color(0xFFEF4444)
                                        else -> Color.Transparent
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(bgColor)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                                    ) {
                                        if (direction == SwipeToDismissBoxValue.StartToEnd) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Habit", tint = Color.White)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Edit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Delete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Habit", tint = Color.White)
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isDone) GamifiedEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isDone) GamifiedEmerald else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(habit.emoji, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = habit.title,
                                                    color = if (isDone) GamifiedEmerald else MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                HabitPillarBadge(category = habit.category)
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Target: ${habit.goalCount} days • 🔥 ${habit.currentStreak}d streak",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Checkbox(
                                        checked = isDone,
                                        onCheckedChange = {
                                            onToggleDay(habit.id, todayStr, isDone)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = GamifiedEmerald,
                                            uncheckedColor = MaterialTheme.colorScheme.outline,
                                            checkmarkColor = Color.White
                                        ),
                                        modifier = Modifier.testTag("today_checkbox_${habit.id}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HabitCheckViewMode.WEEKLY -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { weekOffset-- },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .testTag("prev_week_button")
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Week", tint = MaterialTheme.colorScheme.onSurface)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = monthYearText,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = weekRangeText,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        IconButton(
                            onClick = { weekOffset++ },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .testTag("next_week_button")
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Week", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                habits.forEach { habit ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("habit_card_${habit.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(habit.emoji, fontSize = 18.sp)
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = habit.title,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            HabitPillarBadge(category = habit.category)
                                        }
                                        Text(
                                            text = "Target: ${habit.goalCount} Days",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .background(GamifiedFlameOrange.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                        .border(1.dp, GamifiedFlameOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = "Streak",
                                            tint = GamifiedFlameOrange,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${habit.currentStreak}d",
                                            color = GamifiedFlameOrange,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                currentWeekDays.forEach { date ->
                                    val dateKey = date.toString()
                                    val isToday = date.isEqual(today)
                                    val isChecked = logs.any { it.habitId == habit.id && it.dateString == dateKey && it.completed }

                                    Box(
                                        modifier = Modifier
                                            .size(width = 44.dp, height = 52.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                when {
                                                    isChecked -> GamifiedEmerald.copy(alpha = 0.25f)
                                                    isToday -> GamifiedElectricBlue.copy(alpha = 0.2f)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            )
                                            .border(
                                                width = if (isToday || isChecked) 1.5.dp else 1.dp,
                                                color = when {
                                                    isChecked -> GamifiedEmerald
                                                    isToday -> GamifiedElectricBlue
                                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                if (isToday) {
                                                    onToggleDay(habit.id, dateKey, isChecked)
                                                } else {
                                                    selectedSummaryDate = date
                                                }
                                            }
                                            .testTag("day_capsule_${habit.id}_$dateKey"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = date.format(formatterDayName).uppercase(),
                                                color = if (isChecked) GamifiedEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = date.format(formatterDayNum),
                                                color = if (isChecked) GamifiedEmerald else MaterialTheme.colorScheme.onSurface,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            if (isChecked) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Done",
                                                    tint = GamifiedEmerald,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HabitCheckViewMode.MONTHLY -> {
                MonthlyGoalFulfillmentView(
                    currentMonth = currentMonth,
                    habits = habits,
                    logs = logs,
                    onMonthOffsetChange = { monthOffset += it },
                    onDaySelect = { selectedSummaryDate = it }
                )
            }
        }
    }

    // Day Performance Summary Dialog (Done & Missed Habits for Selected Date)
    selectedSummaryDate?.let { date ->
        DaySummaryDialog(
            date = date,
            habits = habits,
            logs = logs,
            onDismiss = { selectedSummaryDate = null }
        )
    }

    // Edit Habit Dialog
    editingHabit?.let { targetHabit ->
        EditHabitDialog(
            habit = targetHabit,
            onDismiss = { editingHabit = null },
            onConfirm = { updatedHabit ->
                onUpdateHabit(updatedHabit)
                editingHabit = null
            }
        )
    }

    // Delete Confirmation Dialog
    deletingHabit?.let { targetHabit ->
        AlertDialog(
            onDismissRequest = { deletingHabit = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("Delete Habit?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${targetHabit.title}\"? This action will permanently remove this habit.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteHabit(targetHabit.id)
                        deletingHabit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete Habit", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingHabit = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun EditHabitDialog(
    habit: HabitEntity,
    onDismiss: () -> Unit,
    onConfirm: (HabitEntity) -> Unit
) {
    var title by remember { mutableStateOf(habit.title) }
    var emoji by remember { mutableStateOf(habit.emoji) }
    var category by remember { mutableStateOf(habit.category.ifBlank { "Body" }) }
    var goalText by remember { mutableStateOf(habit.goalCount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Edit Habit", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = GamifiedElectricBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_title_input")
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

                // 3 Pillar Badges Selector: Food, Body, Mind
                Text("Habit Badge Pillar", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("Food", "🥑", Color(0xFF10B981)),
                        Triple("Body", "🏋️", Color(0xFF3B82F6)),
                        Triple("Mind", "🧠", Color(0xFF8B5CF6))
                    ).forEach { (badgeName, badgeEmoji, badgeColor) ->
                        val isSelected = category.equals(badgeName, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) badgeColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) badgeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { category = badgeName }
                                .padding(vertical = 10.dp)
                                .testTag("edit_badge_selector_$badgeName"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(badgeEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = badgeName,
                                    color = if (isSelected) badgeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
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
                            habit.copy(
                                title = title,
                                emoji = emoji.ifBlank { "⚡" },
                                category = category,
                                goalCount = goalText.toIntOrNull() ?: habit.goalCount
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GamifiedElectricBlue),
                modifier = Modifier.testTag("save_edit_habit_button")
            ) {
                Text("Save Changes", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun MonthlyGoalFulfillmentView(
    currentMonth: YearMonth,
    habits: List<HabitEntity>,
    logs: List<HabitLogEntity>,
    onMonthOffsetChange: (Int) -> Unit,
    onDaySelect: (LocalDate) -> Unit = {}
) {
    val totalHabits = habits.size.coerceAtLeast(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val monthTitle = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    val today = LocalDate.now()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(GamifiedElectricBlue, GamifiedCyan)),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("monthly_goal_fulfillment_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onMonthOffsetChange(-1) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = MaterialTheme.colorScheme.onSurface)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Goal Fulfillment Status",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = monthTitle,
                        color = GamifiedCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { onMonthOffsetChange(1) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                    Text(
                        text = day,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val firstDayOfMonth = currentMonth.atDay(1)
            val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value - 1

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                var currentDay = 1
                var cellIndex = 0

                while (currentDay <= daysInMonth) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 0..6) {
                            if (cellIndex < dayOfWeekOffset || currentDay > daysInMonth) {
                                Spacer(modifier = Modifier.size(38.dp))
                            } else {
                                val dayDate = currentMonth.atDay(currentDay)
                                val dateKey = dayDate.toString()
                                val isToday = dayDate.isEqual(today)

                                val completedCount = logs.count { it.dateString == dateKey && it.completed }
                                val isFullyCompleted = completedCount >= (totalHabits * 0.7f).coerceAtLeast(1f)

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isFullyCompleted -> GamifiedEmerald
                                                completedCount > 0 -> GamifiedCyan.copy(alpha = 0.3f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        )
                                        .border(
                                            width = if (isToday) 2.dp else if (completedCount > 0) 1.5.dp else 1.dp,
                                            color = when {
                                                isToday -> GamifiedElectricBlue
                                                isFullyCompleted -> GamifiedEmerald
                                                completedCount > 0 -> GamifiedFlameOrange
                                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                            },
                                            shape = CircleShape
                                        )
                                        .clickable { onDaySelect(dayDate) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isFullyCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Fulfilled",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "$currentDay",
                                            color = if (completedCount > 0) GamifiedGold else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                currentDay++
                            }
                            cellIndex++
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(GamifiedEmerald, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Goal Fulfilled", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(GamifiedFlameOrange, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Partial Goal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(GamifiedElectricBlue, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Today", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun DaySummaryDialog(
    date: LocalDate,
    habits: List<HabitEntity>,
    logs: List<HabitLogEntity>,
    onDismiss: () -> Unit
) {
    val dateKey = date.toString()
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
    val dateText = date.format(formatter)

    val completedHabits = habits.filter { h -> logs.any { it.habitId == h.id && it.dateString == dateKey && it.completed } }
    val missedHabits = habits.filter { h -> !logs.any { it.habitId == h.id && it.dateString == dateKey && it.completed } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column {
                Text(
                    text = "Day Performance",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateText,
                    color = GamifiedElectricBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "✅ Completed (${completedHabits.size})",
                        color = GamifiedEmerald,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "❌ Missed (${missedHabits.size})",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (completedHabits.isNotEmpty()) {
                        item {
                            Text(
                                text = "COMPLETED HABITS",
                                color = GamifiedEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                        items(completedHabits) { habit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GamifiedEmerald.copy(alpha = 0.15f))
                                    .border(1.dp, GamifiedEmerald, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(habit.emoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = habit.title,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        HabitPillarBadge(category = habit.category)
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = GamifiedEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (missedHabits.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "MISSED HABITS",
                                color = Color(0xFFEF4444),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                        items(missedHabits) { habit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(habit.emoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = habit.title,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        HabitPillarBadge(category = habit.category)
                                    }
                                }
                                Text("❌", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GamifiedElectricBlue)
            ) {
                Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}
