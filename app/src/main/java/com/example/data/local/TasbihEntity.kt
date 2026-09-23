package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasbih_data")
data class TasbihEntity(
    @PrimaryKey
    val id: String = "current_tasbih",
    val title: String = "সুবহানাল্লাহ",
    val currentCount: Int = 0,
    val targetCount: Int = 33,
    val totalLaps: Int = 0
)
