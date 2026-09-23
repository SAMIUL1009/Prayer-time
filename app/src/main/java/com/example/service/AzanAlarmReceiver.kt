package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.audio.AzanPlayerManager
import com.example.data.local.DailyTrackerEntity
import com.example.data.local.PrayerDatabase
import com.example.data.local.PrayerEntity
import com.example.data.model.CityPreset
import com.example.data.model.PrayerTimeItem
import com.example.data.model.PrayerType
import com.example.util.IslamicDateUtil
import com.example.util.PrayerAlertPreferences
import com.example.util.PrayerCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class AzanAlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AzanAlarmReceiver"
        const val ACTION_TRIGGER_AZAN = "com.example.action.TRIGGER_AZAN"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d(TAG, "onReceive action=$action")

        val prayerName = intent.getStringExtra(AzanScheduler.EXTRA_PRAYER_NAME) ?: "ওয়াক্ত"
        val prayerId = intent.getStringExtra(AzanScheduler.EXTRA_PRAYER_ID) ?: ""

        when (action) {
            AzanNotificationHelper.ACTION_STOP_AZAN -> {
                AzanPlayerManager.stopAzan()
                AzanNotificationHelper.dismissNotification(context, prayerId)
            }

            AzanNotificationHelper.ACTION_MARK_PRAYED -> {
                AzanPlayerManager.stopAzan()
                AzanNotificationHelper.dismissNotification(context, prayerId)

                // Mark as completed in tracker database
                if (prayerId.isNotEmpty()) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = PrayerDatabase.getDatabase(context)
                            val todayKey = IslamicDateUtil.getTodayDateKey()
                            db.prayerDao().insertOrUpdateDailyRecord(
                                DailyTrackerEntity(
                                    dateString = todayKey,
                                    prayerId = prayerId,
                                    isCompleted = true,
                                    completedTimestamp = System.currentTimeMillis()
                                )
                            )
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(
                                    context,
                                    "মাশাআল্লাহ! $prayerName নামাজ আদায়ের তথ্য সংরক্ষিত হয়েছে।",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to mark prayer as completed", e)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }

            ACTION_TRIGGER_AZAN -> {
                // 1. Play Azan sound if enabled in preferences
                val isAudioEnabled = PrayerAlertPreferences.isAudioAzanEnabled(context)
                if (isAudioEnabled) {
                    AzanPlayerManager.playAzan(context, prayerName)
                }

                // 2. Trigger high-priority heads-up local notification
                AzanNotificationHelper.showAzanNotification(context, prayerName, prayerId)

                // 3. Reschedule alarms for upcoming days
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = PrayerDatabase.getDatabase(context)
                        val settings: List<PrayerEntity> = db.prayerDao().getAllPrayerSettings().first()
                        val settingsMap = settings.associateBy { it.prayerId }

                        val city = CityPreset.westBengalDistricts.first()
                        val nowCal = Calendar.getInstance()
                        val calc = PrayerCalculator.calculateForDate(nowCal, city.latitude, city.longitude, city.timeZone)

                        val items = PrayerType.allSchedulePrayers.map { type ->
                            val s = settingsMap[type.id]
                            val baseMins = when (type) {
                                PrayerType.FAJR -> calc.fajrMinutes
                                PrayerType.SUNRISE -> calc.sunriseMinutes
                                PrayerType.DHUHR -> calc.dhuhrMinutes
                                PrayerType.ASR -> calc.asrMinutes
                                PrayerType.MAGHRIB -> calc.maghribMinutes
                                PrayerType.ISHA -> calc.ishaMinutes
                                PrayerType.TAHAJJUD -> calc.tahajjudMinutes
                            }
                            val userOffset = s?.offsetMinutes ?: 0
                            val customH = s?.customHour
                            val customM = s?.customMinute

                            val effectiveMins = if (customH != null && customM != null) {
                                (customH * 60 + customM + userOffset + 1440) % 1440
                            } else {
                                (baseMins + userOffset + 1440) % 1440
                            }

                            PrayerTimeItem(
                                type = type,
                                effectiveHour = (effectiveMins / 60) % 24,
                                effectiveMinute = (effectiveMins % 60 + 60) % 60,
                                defaultHour = (baseMins / 60) % 24,
                                defaultMinute = (baseMins % 60 + 60) % 60,
                                customHour = customH,
                                customMinute = customM,
                                offsetMinutes = userOffset,
                                jamatHour = s?.jamatHour,
                                jamatMinute = s?.jamatMinute,
                                isNotificationEnabled = s?.isNotificationEnabled ?: true,
                                isCompletedToday = false
                            )
                        }

                        AzanScheduler.scheduleAllAzans(context, items)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error rescheduling Azan alarms", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
