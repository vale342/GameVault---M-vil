package com.example.gamevault.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WishEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wishDao(): WishDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "gamevault_db"
                ).build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}