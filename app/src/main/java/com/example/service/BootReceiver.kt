package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.PrayerDatabase
import com.example.data.local.PrayerEntity
import com.example.data.model.CityPreset
import com.example.data.model.PrayerTimeItem
import com.example.data.model.PrayerType
import com.example.util.PrayerCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d(TAG, "onReceive action=$action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == "android.intent.action.TIME_SET" ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
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
                    Log.d(TAG, "Successfully rescheduled all prayer alarms after $action")
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling alarms in BootReceiver", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
