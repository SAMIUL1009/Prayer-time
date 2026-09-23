package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_settings")
    fun getAllPrayerSettings(): Flow<List<PrayerEntity>>

    @Query("SELECT * FROM prayer_settings WHERE prayerId = :id LIMIT 1")
    suspend fun getPrayerSettingById(id: String): PrayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePrayerSetting(entity: PrayerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPrayerSettings(entities: List<PrayerEntity>)

    @Query("DELETE FROM prayer_settings")
    suspend fun clearAllPrayerSettings()

    // Daily Tracker queries
    @Query("SELECT * FROM daily_prayer_records WHERE dateString = :dateString")
    fun getRecordsForDate(dateString: String): Flow<List<DailyTrackerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyRecord(record: DailyTrackerEntity)

    @Query("SELECT COUNT(*) FROM daily_prayer_records WHERE isCompleted = 1")
    fun getTotalCompletedPrayersCount(): Flow<Int>

    // Tasbih queries
    @Query("SELECT * FROM tasbih_data WHERE id = :id LIMIT 1")
    fun getTasbihFlow(id: String = "current_tasbih"): Flow<TasbihEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTasbih(tasbih: TasbihEntity)
}
