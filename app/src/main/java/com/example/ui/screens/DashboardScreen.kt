package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.GamifiedAchievementsRow
import com.example.ui.components.GamifiedLevelHeader
import com.example.ui.components.GamifiedStreakFlameCard
import com.example.ui.components.MicroGardenBanner
import com.example.ui.components.ProgressLineChartSummary
import com.example.ui.components.TodayTaskCompletionWidget
import com.example.ui.components.TrellisBrandedProgressLogo
import com.example.ui.viewmodel.HabitFlowViewModel
import java.time.LocalDate

@Composable
fun DashboardScreen(
    viewModel: HabitFlowViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habits.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val logs by viewModel.habitLogs.collectAsState()

    // Real dynamic current date
    val todayKey = remember { LocalDate.now().toString() }
    val totalLogsCount = logs.count { it.completed }
    val completedTasksCount = tasks.count { it.status.equals("Done", ignoreCase = true) }
    val totalXp = (totalLogsCount * 50) + (completedTasksCount * 25)

    val bestStreak = habits.maxOfOrNull { it.bestStreak } ?: 0
    val activeStreak = habits.maxOfOrNull { it.currentStreak } ?: 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Trellis Creative Architectural Logo Header
        item {
            TrellisBrandedProgressLogo(
                tasks = tasks,
                habits = habits,
                logs = logs
            )
        }

        // Sleek Micro Garden Banner (auto-watered when habits are completed)
        item {
            MicroGardenBanner(
                totalXp = totalXp
            )
        }

        // Level & XP Banner
        item {
            GamifiedLevelHeader(
                totalXp = totalXp,
                completedCount = totalLogsCount
            )
        }

        // Today's Circular Completion Progress Widget
        item {
            TodayTaskCompletionWidget(
                tasks = tasks,
                habits = habits,
                logs = logs
            )
        }

        // Streak Flame Card
        item {
            GamifiedStreakFlameCard(
                currentStreak = activeStreak,
                bestStreak = bestStreak
            )
        }

        // Progress Line Graph Summary
        item {
            ProgressLineChartSummary(
                habits = habits,
                logs = logs
            )
        }

        // Achievements & Badges
        item {
            GamifiedAchievementsRow()
        }
    }
}
