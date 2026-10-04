package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

    fun addHabit(title: String, emoji: String, category: String, goalCount: Int) {
        viewModelScope.launch {
            repository.addHabit(title, emoji, category, goalCount)
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
