package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_prayer_records",
    indices = [Index(value = ["dateString", "prayerId"], unique = true)]
)
data class DailyTrackerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // Format: YYYY-MM-DD
    val prayerId: String,
    val isCompleted: Boolean,
    val completedTimestamp: Long = System.currentTimeMillis()
)
