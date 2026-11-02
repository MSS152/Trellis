package com.example.data.health

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

data class HealthDataSnapshot(
    val steps: Int = 0,
    val activeDistanceKm: Double = 0.0,
    val isConnected: Boolean = true,
    val lastSyncTime: String = "Just now"
)

class HealthConnectManager(private val context: Context) {

    private val _healthSnapshot = MutableStateFlow(
        HealthDataSnapshot(
            steps = 8450,
            activeDistanceKm = 6.2,
            isConnected = true,
            lastSyncTime = "Synced with Samsung Health"
        )
    )
    val healthSnapshot: StateFlow<HealthDataSnapshot> = _healthSnapshot.asStateFlow()

    fun syncDataNow(mockSteps: Int? = null, mockDistance: Double? = null) {
        val current = _healthSnapshot.value
        val newSteps = mockSteps ?: (current.steps + 250)
        val newDist = mockDistance ?: (current.activeDistanceKm + 0.2)
        
        _healthSnapshot.value = current.copy(
            steps = newSteps,
            activeDistanceKm = String.format("%.1f", newDist).toDouble(),
            lastSyncTime = "Updated ${LocalDate.now()}"
        )
    }

    fun updateStepProgress(addSteps: Int) {
        val current = _healthSnapshot.value
        _healthSnapshot.value = current.copy(
            steps = current.steps + addSteps,
            activeDistanceKm = String.format("%.1f", current.activeDistanceKm + (addSteps * 0.0008)).toDouble(),
            lastSyncTime = "Synced just now"
        )
    }
}
