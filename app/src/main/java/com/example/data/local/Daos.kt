package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE spaceId = :spaceId ORDER BY id ASC")
    fun getHabitsForSpace(spaceId: String): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: Long)

    @Query("SELECT * FROM habit_logs WHERE dateString = :dateString")
    fun getLogsForDate(dateString: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs")
    fun getAllHabitLogs(): Flow<List<HabitLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteLog(habitId: Long, dateString: String)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE spaceId = :spaceId ORDER BY id DESC")
    fun getTasksForSpace(spaceId: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)
}

@Dao
interface CollaborationDao {
    @Query("SELECT * FROM collaborators WHERE spaceId = :spaceId")
    fun getCollaboratorsForSpace(spaceId: String): Flow<List<CollaboratorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollaborator(collaborator: CollaboratorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollaborators(collaborators: List<CollaboratorEntity>)

    @Query("SELECT * FROM activity_logs WHERE spaceId = :spaceId ORDER BY timestamp DESC LIMIT 50")
    fun getActivityLogsForSpace(spaceId: String): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLogEntity)

    @Query("SELECT * FROM collaborative_spaces WHERE spaceId = :spaceId")
    suspend fun getSpaceById(spaceId: String): CollaborativeSpaceEntity?

    @Query("SELECT * FROM collaborative_spaces")
    fun getAllSpaces(): Flow<List<CollaborativeSpaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpace(space: CollaborativeSpaceEntity)
}
