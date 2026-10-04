package com.example.data.repository

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.local.ActivityLogEntity
import com.example.data.local.CollaborationDao
import com.example.data.local.CollaborativeSpaceEntity
import com.example.data.local.CollaboratorEntity
import com.example.data.local.HabitDao
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.local.TaskDao
import com.example.data.local.TaskEntity
import com.example.data.remote.gemini.Content
import com.example.data.remote.gemini.GeminiClient
import com.example.data.remote.gemini.GenerateContentRequest
import com.example.data.remote.gemini.GenerationConfig
import com.example.data.remote.gemini.InlineData
import com.example.data.remote.gemini.Part
import com.example.data.remote.gemini.ThinkingConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import kotlin.random.Random

class HabitRepository(
    private val habitDao: HabitDao,
    private val taskDao: TaskDao,
    private val collaborationDao: CollaborationDao
) {
    private val _isOnlineMode = MutableStateFlow(true)
    val isOnlineMode: StateFlow<Boolean> = _isOnlineMode.asStateFlow()

    private val _currentSpaceId = MutableStateFlow("team_habit_flow")
    val currentSpaceId: StateFlow<String> = _currentSpaceId.asStateFlow()

    fun setOnlineMode(enabled: Boolean) {
        _isOnlineMode.value = enabled
    }

    fun setSpaceId(spaceId: String) {
        _currentSpaceId.value = spaceId
    }

    // Flows from Room Database
    fun getHabits(spaceId: String): Flow<List<HabitEntity>> = habitDao.getHabitsForSpace(spaceId)
    fun getTasks(spaceId: String): Flow<List<TaskEntity>> = taskDao.getTasksForSpace(spaceId)
    fun getCollaborators(spaceId: String): Flow<List<CollaboratorEntity>> = collaborationDao.getCollaboratorsForSpace(spaceId)
    fun getActivityLogs(spaceId: String): Flow<List<ActivityLogEntity>> = collaborationDao.getActivityLogsForSpace(spaceId)
    val allLogs: Flow<List<HabitLogEntity>> = habitDao.getAllHabitLogs()
    val allSpaces: Flow<List<CollaborativeSpaceEntity>> = collaborationDao.getAllSpaces()

    suspend fun toggleHabitLog(habitId: Long, dateString: String, completed: Boolean) {
        if (completed) {
            habitDao.insertOrUpdateLog(
                HabitLogEntity(
                    habitId = habitId,
                    dateString = dateString,
                    completed = true,
                    updatedBy = "You"
                )
            )
            collaborationDao.insertActivityLog(
                ActivityLogEntity(
                    userName = "You",
                    userEmoji = "⚡",
                    actionText = "Completed habit for $dateString",
                    spaceId = _currentSpaceId.value
                )
            )
        } else {
            habitDao.deleteLog(habitId, dateString)
        }
    }

    suspend fun addHabit(title: String, emoji: String, category: String, goalCount: Int) {
        val habit = HabitEntity(
            title = title,
            emoji = emoji,
            category = category,
            goalCount = goalCount,
            spaceId = _currentSpaceId.value
        )
        habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habitId: Long) {
        habitDao.deleteHabitById(habitId)
    }

    suspend fun addTask(title: String, description: String, priority: String, assignedTo: String, dueDate: String) {
        val task = TaskEntity(
            title = title,
            description = description,
            priority = priority,
            assignedTo = assignedTo,
            dueDate = dueDate,
            spaceId = _currentSpaceId.value
        )
        taskDao.insertTask(task)
    }

    suspend fun updateTaskStatus(task: TaskEntity, newStatus: String) {
        val updated = task.copy(status = newStatus)
        taskDao.updateTask(updated)
    }

    // Seed default data if database is empty
    suspend fun seedInitialDataIfEmpty() {
        val existingHabits = habitDao.getHabitsForSpace("team_habit_flow").first()
        if (existingHabits.isEmpty()) {
            val defaultHabits = listOf(
                HabitEntity(id = 1, title = "Wake up at 6AM", emoji = "⏰", category = "Body", goalCount = 30, currentStreak = 7, bestStreak = 14, spaceId = "team_habit_flow"),
                HabitEntity(id = 2, title = "No Snoozing", emoji = "🚫", category = "Mind", goalCount = 30, currentStreak = 5, bestStreak = 10, spaceId = "team_habit_flow"),
                HabitEntity(id = 3, title = "Drink 3L Water", emoji = "💧", category = "Food", goalCount = 30, currentStreak = 8, bestStreak = 18, spaceId = "team_habit_flow"),
                HabitEntity(id = 4, title = "Gym Workout", emoji = "🏋️", category = "Body", goalCount = 20, currentStreak = 4, bestStreak = 9, spaceId = "team_habit_flow"),
                HabitEntity(id = 5, title = "Healthy Meal Prep", emoji = "🥗", category = "Food", goalCount = 20, currentStreak = 6, bestStreak = 8, spaceId = "team_habit_flow"),
                HabitEntity(id = 6, title = "Read 10 Pages", emoji = "📚", category = "Mind", goalCount = 30, currentStreak = 14, bestStreak = 21, spaceId = "team_habit_flow"),
                HabitEntity(id = 7, title = "Meditation", emoji = "🧘‍♀️", category = "Mind", goalCount = 30, currentStreak = 6, bestStreak = 12, spaceId = "team_habit_flow"),
                HabitEntity(id = 8, title = "Stretching Routine", emoji = "🧘", category = "Body", goalCount = 30, currentStreak = 10, bestStreak = 15, spaceId = "team_habit_flow")
            )
            habitDao.insertHabits(defaultHabits)

            // Seed habit logs using real ISO dates (java.time.LocalDate) for past 60 days
            val today = LocalDate.now()
            val initialLogs = mutableListOf<HabitLogEntity>()

            for (daysAgo in 0..60) {
                val dateStr = today.minusDays(daysAgo.toLong()).toString()
                for (habit in defaultHabits) {
                    // Seed ~75% completion rate for realistic line graph trends
                    val shouldComplete = (daysAgo % 2 == 0) || (daysAgo % 3 == 0) || (habit.id % 2 == 0L)
                    if (shouldComplete) {
                        initialLogs.add(
                            HabitLogEntity(
                                habitId = habit.id,
                                dateString = dateStr,
                                completed = true,
                                updatedBy = "You"
                            )
                        )
                    }
                }
            }

            for (log in initialLogs) {
                habitDao.insertOrUpdateLog(log)
            }

            // Seed Tasks
            taskDao.insertTask(
                TaskEntity(
                    title = "Review Weekly Progress Chart",
                    description = "Analyze habit completion rate with line graph trends",
                    priority = "High",
                    status = "In Progress",
                    assignedTo = "You",
                    category = "Strategy",
                    dueDate = "Today",
                    spaceId = "team_habit_flow"
                )
            )
            taskDao.insertTask(
                TaskEntity(
                    title = "Set Up Monthly Habit Goals",
                    description = "Ensure 30-day target goals are active",
                    priority = "Medium",
                    status = "Done",
                    assignedTo = "You",
                    category = "Planning",
                    dueDate = "Yesterday",
                    spaceId = "team_habit_flow"
                )
            )
        }
    }
}
