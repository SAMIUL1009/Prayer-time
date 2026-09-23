package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_settings")
data class PrayerEntity(
    @PrimaryKey
    val prayerId: String,
    val customHour: Int? = null,
    val customMinute: Int? = null,
    val offsetMinutes: Int = 0,
    val jamatHour: Int? = null,
    val jamatMinute: Int? = null,
    val isNotificationEnabled: Boolean = true
)
