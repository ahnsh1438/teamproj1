package com.example.dopamindetox.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BlockedApp(
    @PrimaryKey val packageName: String,
    val label: String
)

@Entity
data class Goal(
    @PrimaryKey val id: Int = 0,
    val minutes: Int
)

@Entity
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val completed: Boolean = false,
    val completedAt: String? = null
)

@Entity
data class AltActivity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String
)

@Entity
data class UsageSample(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val minutes: Int,
    val dayKey: String // yyyyMMdd
)

@Entity
data class ScreenEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // SCREEN_ON
    val ts: Long
)

@Entity
data class Reward(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayKey: String,
    val achieved: Boolean
)
