package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val emoji: String,
    val category: String = "Health",
    val goalCount: Int = 30, // Target goal days or total goal
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val spaceId: String = "default_workspace",
    val createdAt: Long = System.currentTimeMillis(),
    
    // Anti-Burnout, Negative Habit & Flexible Frequency Extensions
    val habitType: String = "POSITIVE", // "POSITIVE" (Build good habit) vs "NEGATIVE" (Break bad habit)
    val targetFrequency: String = "DAILY", // "DAILY" vs "WEEKLY_X_TIMES"
    val weeklyTargetCount: Int = 7, // e.g. 3 for "3x a week", 7 for daily
    val unitType: String = "COMPLETION", // "COMPLETION", "STEPS", "KM", "MINUTES", "PAGES", "REPS", "INTENSITY_SCALE"
    val targetUnitValue: Double = 1.0, // e.g. 10000.0 steps, 5.0 km, 20 pages
    val unitLabel: String = "check", // "steps", "km", "mins", "pages", "reps", "pts"
    val momentumScore: Int = 100, // Anti-burnout momentum score (0-100%)
    val streakShields: Int = 1, // Number of available Streak Shields
    val isHealthConnectSynced: Boolean = false, // Auto-syncs with Health Connect if true
    val healthMetricType: String = "NONE" // "NONE", "STEPS", "DISTANCE"
)

@Entity(tableName = "habit_logs", primaryKeys = ["habitId", "dateString"])
data class HabitLogEntity(
    val habitId: Long,
    val dateString: String, // e.g. "2026-11-01"
    val completed: Boolean = false,
    val notes: String? = null,
    val updatedBy: String = "You",
    val timestamp: Long = System.currentTimeMillis(),
    
    // Variable Metric & Forgiveness Logging
    val numericValue: Double = 1.0, // e.g. 8,500 steps or 15 pages logged
    val intensityRating: Int = 5, // 1-10 effort intensity scale
    val isShieldUsed: Boolean = false, // True if a Streak Shield protected this missed day
    val isCleanDay: Boolean = true // For negative habits, true = clean/abstained day
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = "Medium", // High, Medium, Low
    val status: String = "Todo", // Todo, In Progress, Done
    val assignedTo: String = "Unassigned",
    val category: String = "General",
    val dueDate: String = "Today",
    val spaceId: String = "default_workspace",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "collaborators")
data class CollaboratorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val avatarEmoji: String,
    val role: String,
    val status: String = "Online", // Online, Offline, Busy
    val spaceId: String = "default_workspace",
    val lastActive: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userName: String,
    val userEmoji: String = "👤",
    val actionText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val spaceId: String = "default_workspace"
)

@Entity(tableName = "collaborative_spaces")
data class CollaborativeSpaceEntity(
    @PrimaryKey val spaceId: String,
    val name: String,
    val joinCode: String,
    val memberCount: Int = 1,
    val isSyncEnabled: Boolean = true
)

@Entity(tableName = "garden_state")
data class GardenStateEntity(
    @PrimaryKey val id: Int = 1,
    val gardenLevel: Int = 1,
    val totalGardenXp: Int = 0,
    val sproutStage: String = "SPROUT", // "SEED", "SPROUT", "SAPLING", "FLOWERING_TREE", "ANCIENT_SANCTUARY"
    val floraCount: Int = 1,
    val gardenHealthScore: Int = 100, // 0-100%
    val lastNurturedDate: String = ""
)
