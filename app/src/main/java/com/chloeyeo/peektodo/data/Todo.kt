package com.chloeyeo.peektodo.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "is_done")
    val isDone: Boolean = false,
    /** Epoch millis. */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    /** Epoch millis. */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
