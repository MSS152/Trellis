package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.health.HealthDataSnapshot
import com.example.data.local.GardenStateEntity
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.data.local.TaskEntity
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HabitFlowViewModel(private val repository: HabitRepository) : ViewModel() {

    val currentSpaceId: StateFlow<String> = repository.currentSpaceId

    val habits: StateFlow<List<HabitEntity>> = currentSpaceId.flatMapLatest { spaceId ->
        repository.getHabits(spaceId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tasks: StateFlow<List<TaskEntity>> = currentSpaceId.flatMapLatest { spaceId ->
        repository.getTasks(spaceId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val habitLogs: StateFlow<List<HabitLogEntity>> = repository.allLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val gardenState: StateFlow<GardenStateEntity?> = repository.gardenState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GardenStateEntity(id = 1, gardenLevel = 1, totalGardenXp = 0, sproutStage = "SPROUT", floraCount = 1, gardenHealthScore = 100)
    )

    val healthSnapshot: StateFlow<HealthDataSnapshot> = repository.healthConnectManager.healthSnapshot

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun toggleHabit(habitId: Long, dateString: String, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleHabitLog(habitId, dateString, isCompleted)
        }
    }

    fun toggleHabitWithDetails(
        habitId: Long,
        dateString: String,
        isCompleted: Boolean,
        numericValue: Double = 1.0,
        intensityRating: Int = 5,
        notes: String? = null
    ) {
        viewModelScope.launch {
            repository.toggleHabitLog(
                habitId = habitId,
                dateString = dateString,
                completed = isCompleted,
                numericValue = numericValue,
                intensityRating = intensityRating,
                notes = notes
            )
        }
    }

    fun addHabit(
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
        viewModelScope.launch {
            repository.addHabit(
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
                healthMetricType = healthMetricType
            )
        }
    }

    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    fun addTask(title: String, description: String, priority: String, assignedTo: String, dueDate: String) {
        viewModelScope.launch {
            repository.addTask(title, description, priority, assignedTo, dueDate)
        }
    }

    fun updateTaskStatus(task: TaskEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateTaskStatus(task, newStatus)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun nurtureGarden() {
        viewModelScope.launch {
            repository.nurtureGarden()
        }
    }

    fun syncHealthConnect() {
        viewModelScope.launch {
            repository.syncHealthConnect()
        }
    }
}

class HabitFlowViewModelFactory(private val repository: HabitRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitFlowViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitFlowViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
