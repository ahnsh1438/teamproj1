package com.example.dopamindetox.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BlockedApp::class, Goal::class, Todo::class, AltActivity::class,
        UsageSample::class, ScreenEvent::class, Reward::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedApp(): BlockedAppDao
    abstract fun goal(): GoalDao
    abstract fun todo(): TodoDao
    abstract fun activity(): ActivityDao
    abstract fun usage(): UsageDao
    abstract fun screenEvent(): ScreenEventDao
    abstract fun reward(): RewardDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(ctx: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(ctx, AppDatabase::class.java, "dopa.db")
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
        }
    }
}
