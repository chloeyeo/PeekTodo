package com.chloeyeo.peektodo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Todo::class], version = 1, exportSchema = false)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun todoDao(): TodoDao

    companion object {
        private const val NAME = "peektodo.db"

        fun build(context: Context): TodoDatabase =
            Room.databaseBuilder(context.applicationContext, TodoDatabase::class.java, NAME)
                .build()
    }
}
