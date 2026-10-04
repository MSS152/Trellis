package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val emoji: String,
    val category: String = "Health",
    val goalCount: Int = 30, // e.g., 30 days goal as seen in screenshot
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val spaceId: String = "default_workspace",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit_logs", primaryKeys = ["habitId", "dateString"])
data class HabitLogEntity(
    val habitId: Long,
    val dateString: String, // e.g., "2026-10-01" or "W1D1"
    val completed: Boolean = false,
    val notes: String? = null,
    val updatedBy: String = "You",
    val timestamp: Long = System.currentTimeMillis()
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
