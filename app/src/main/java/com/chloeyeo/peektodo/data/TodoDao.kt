package com.chloeyeo.peektodo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    /** Open tasks first (newest on top), then completed tasks. */
    @Query("SELECT * FROM todos ORDER BY is_done ASC, created_at DESC")
    fun observeAll(): Flow<List<Todo>>

    /** Only open tasks, newest first. Drives the lock-screen notification. */
    @Query("SELECT * FROM todos WHERE is_done = 0 ORDER BY created_at DESC")
    fun observeOpen(): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE is_done = 0 ORDER BY created_at DESC")
    suspend fun getOpen(): List<Todo>

    @Insert
    suspend fun insert(todo: Todo): Long

    @Update
    suspend fun update(todo: Todo)

    @Delete
    suspend fun delete(todo: Todo)

    @Query("UPDATE todos SET is_done = :done, updated_at = :updatedAt WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean, updatedAt: Long)
}
