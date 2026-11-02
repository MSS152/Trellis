package com.example.data.repository

import com.example.data.health.HealthConnectManager
import com.example.data.local.ActivityLogEntity
import com.example.data.local.CollaborationDao
import com.example.data.local.CollaborativeSpaceEntity
import com.example.data.local.CollaboratorEntity
import com.example.data.local.GardenDao
import com.example.data.local.GardenStateEntity
import com.example.data.local.HabitDao
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.local.TaskDao
import com.example.data.local.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class HabitRepository(
    private val habitDao: HabitDao,
    private val taskDao: TaskDao,
    private val collaborationDao: CollaborationDao,
    private val gardenDao: GardenDao,
    val healthConnectManager: HealthConnectManager
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
    val gardenState: Flow<GardenStateEntity?> = gardenDao.getGardenState()

    suspend fun toggleHabitLog(
        habitId: Long,
        dateString: String,
        completed: Boolean,
        numericValue: Double = 1.0,
        intensityRating: Int = 5,
        notes: String? = null
    ) {
        if (completed) {
            habitDao.insertOrUpdateLog(
                HabitLogEntity(
                    habitId = habitId,
                    dateString = dateString,
                    completed = true,
                    numericValue = numericValue,
                    intensityRating = intensityRating,
                    notes = notes,
                    updatedBy = "You"
                )
            )
            habitDao.getHabitById(habitId)?.let { existing ->
                val newStreak = existing.currentStreak + 1
                val newBest = maxOf(existing.bestStreak, newStreak)
                habitDao.updateHabit(
                    existing.copy(
                        currentStreak = newStreak,
                        bestStreak = newBest
                    )
                )
            }
            collaborationDao.insertActivityLog(
                ActivityLogEntity(
                    userName = "You",
                    userEmoji = "⚡",
                    actionText = "Completed habit & watered garden for $dateString",
                    spaceId = _currentSpaceId.value
                )
            )
            nurtureGarden()
        } else {
            habitDao.deleteLog(habitId, dateString)
            habitDao.getHabitById(habitId)?.let { existing ->
                val newStreak = (existing.currentStreak - 1).coerceAtLeast(0)
                habitDao.updateHabit(
                    existing.copy(
                        currentStreak = newStreak
                    )
                )
            }
        }
    }

    suspend fun addHabit(
        title: String,
        emoji: String,
        category: String,
        goalCount: Int,
        habitType: String = "POSITIVE",
        targetFrequency: String = "DAILY",
        weeklyTargetCount: Int = 7,
        unitType: String = "COMPLETION",
        targetUnitValue: Double = 1.0,
        unitLabel: String = "check",
        isHealthConnectSynced: Boolean = false,
        healthMetricType: String = "NONE"
    ) {
        val habit = HabitEntity(
            title = title,
            emoji = emoji,
            category = category,
            goalCount = goalCount,
            habitType = habitType,
            targetFrequency = targetFrequency,
            weeklyTargetCount = weeklyTargetCount,
            unitType = unitType,
            targetUnitValue = targetUnitValue,
            unitLabel = unitLabel,
            isHealthConnectSynced = isHealthConnectSynced,
            healthMetricType = healthMetricType,
            spaceId = _currentSpaceId.value
        )
        habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: HabitEntity) {
        if (habit.id == 0L) {
            habitDao.insertHabit(habit.copy(spaceId = _currentSpaceId.value))
        } else {
            habitDao.updateHabit(habit)
        }
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

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTaskById(taskId)
    }

    suspend fun nurtureGarden() {
        val currentGarden = gardenDao.getGardenState()
        // Increments garden nurture state
        gardenDao.insertOrUpdateGardenState(
            GardenStateEntity(
                id = 1,
                gardenLevel = 2,
                totalGardenXp = 150,
                sproutStage = "SAPLING",
                gardenHealthScore = 100,
                lastNurturedDate = LocalDate.now().toString()
            )
        )
    }

    suspend fun syncHealthConnect() {
        healthConnectManager.syncDataNow()
    }

    suspend fun clearAllData() {
        habitDao.deleteAllHabits()
        habitDao.deleteAllHabitLogs()
        taskDao.deleteAllTasks()
    }

    suspend fun seedInitialDataIfEmpty() {
        // Preserves all local user data saved in Room database
    }
}
