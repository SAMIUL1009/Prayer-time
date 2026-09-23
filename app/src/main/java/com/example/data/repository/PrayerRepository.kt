package com.example.data.repository

import com.example.data.local.DailyTrackerEntity
import com.example.data.local.PrayerDao
import com.example.data.local.PrayerEntity
import com.example.data.local.TasbihEntity
import com.example.data.model.PrayerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PrayerRepository(private val dao: PrayerDao) {

    val prayerSettingsFlow: Flow<List<PrayerEntity>> = dao.getAllPrayerSettings()
    val tasbihFlow: Flow<TasbihEntity?> = dao.getTasbihFlow()

    suspend fun ensureDefaultSettings() = withContext(Dispatchers.IO) {
        val existing = dao.getPrayerSettingById(PrayerType.FAJR.id)
        if (existing == null) {
            val defaults = PrayerType.entries.map { type ->
                PrayerEntity(
                    prayerId = type.id,
                    customHour = null,
                    customMinute = null,
                    offsetMinutes = 0,
                    jamatHour = type.defaultJamatHour,
                    jamatMinute = type.defaultJamatMinute,
                    isNotificationEnabled = true
                )
            }
            dao.insertAllPrayerSettings(defaults)
        }
    }

    suspend fun updateCustomTime(prayerId: String, hour: Int, minute: Int) = withContext(Dispatchers.IO) {
        val current = dao.getPrayerSettingById(prayerId) ?: PrayerEntity(prayerId = prayerId)
        val updated = current.copy(
            customHour = hour,
            customMinute = minute
        )
        dao.insertOrUpdatePrayerSetting(updated)
    }

    suspend fun updateJamatTime(prayerId: String, hour: Int?, minute: Int?) = withContext(Dispatchers.IO) {
        val current = dao.getPrayerSettingById(prayerId) ?: PrayerEntity(prayerId = prayerId)
        val updated = current.copy(
            jamatHour = hour,
            jamatMinute = minute
        )
        dao.insertOrUpdatePrayerSetting(updated)
    }

    suspend fun adjustOffset(prayerId: String, deltaMinutes: Int) = withContext(Dispatchers.IO) {
        val current = dao.getPrayerSettingById(prayerId) ?: PrayerEntity(prayerId = prayerId)
        val newOffset = current.offsetMinutes + deltaMinutes
        val updated = current.copy(offsetMinutes = newOffset)
        dao.insertOrUpdatePrayerSetting(updated)
    }

    suspend fun resetPrayerToDefault(prayerId: String) = withContext(Dispatchers.IO) {
        val prayerType = PrayerType.fromId(prayerId)
        val current = dao.getPrayerSettingById(prayerId) ?: PrayerEntity(prayerId = prayerId)
        val updated = current.copy(
            customHour = null,
            customMinute = null,
            offsetMinutes = 0,
            jamatHour = prayerType.defaultJamatHour,
            jamatMinute = prayerType.defaultJamatMinute
        )
        dao.insertOrUpdatePrayerSetting(updated)
    }

    suspend fun resetAllToDefault() = withContext(Dispatchers.IO) {
        val defaults = PrayerType.entries.map { type ->
            PrayerEntity(
                prayerId = type.id,
                customHour = null,
                customMinute = null,
                offsetMinutes = 0,
                jamatHour = type.defaultJamatHour,
                jamatMinute = type.defaultJamatMinute,
                isNotificationEnabled = true
            )
        }
        dao.clearAllPrayerSettings()
        dao.insertAllPrayerSettings(defaults)
    }

    suspend fun toggleNotification(prayerId: String) = withContext(Dispatchers.IO) {
        val current = dao.getPrayerSettingById(prayerId) ?: PrayerEntity(prayerId = prayerId)
        val updated = current.copy(isNotificationEnabled = !current.isNotificationEnabled)
        dao.insertOrUpdatePrayerSetting(updated)
    }

    fun getRecordsForDate(dateKey: String): Flow<List<DailyTrackerEntity>> {
        return dao.getRecordsForDate(dateKey)
    }

    suspend fun togglePrayerCompleted(dateKey: String, prayerId: String, completed: Boolean) = withContext(Dispatchers.IO) {
        val record = DailyTrackerEntity(
            dateString = dateKey,
            prayerId = prayerId,
            isCompleted = completed,
            completedTimestamp = System.currentTimeMillis()
        )
        dao.insertOrUpdateDailyRecord(record)
    }

    suspend fun saveTasbih(tasbih: TasbihEntity) = withContext(Dispatchers.IO) {
        dao.saveTasbih(tasbih)
    }
}
