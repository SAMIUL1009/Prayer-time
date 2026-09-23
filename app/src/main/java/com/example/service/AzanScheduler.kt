package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.PrayerTimeItem
import com.example.data.model.PrayerType
import java.util.Calendar

object AzanScheduler {
    private const val TAG = "AzanScheduler"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_PRAYER_ID = "extra_prayer_id"

    fun getRequestCode(type: PrayerType): Int {
        return 1000 + type.ordinal
    }

    /**
     * Schedules exact or inexact AlarmManager alarms for all configured prayer times.
     * Each prayer has its own distinct PendingIntent and request code.
     */
    fun scheduleAllAzans(context: Context, items: List<PrayerTimeItem>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = Calendar.getInstance()

        // Create notification channel beforehand
        AzanNotificationHelper.createNotificationChannel(context)

        val canExact = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
        } catch (_: Throwable) {
            false
        }

        // Loop through each prayer
        for (item in items) {
            // We schedule Fard prayers (Fajr, Dhuhr, Asr, Maghrib, Isha) and optionally Tahajjud
            if (item.type == PrayerType.SUNRISE) continue

            val requestCode = getRequestCode(item.type)
            val intent = Intent(context, AzanAlarmReceiver::class.java).apply {
                action = AzanAlarmReceiver.ACTION_TRIGGER_AZAN
                putExtra(EXTRA_PRAYER_NAME, item.type.banglaName)
                putExtra(EXTRA_PRAYER_ID, item.type.id)
            }

            if (item.isNotificationEnabled) {
                val prayerCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, item.effectiveHour)
                    set(Calendar.MINUTE, item.effectiveMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // If time has already passed today, schedule for tomorrow
                if (prayerCal.timeInMillis <= now.timeInMillis) {
                    prayerCal.add(Calendar.DAY_OF_YEAR, 1)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                var success = false
                if (canExact) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                prayerCal.timeInMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                prayerCal.timeInMillis,
                                pendingIntent
                            )
                        }
                        success = true
                        Log.d(TAG, "Exact alarm set for ${item.type.banglaName} at ${prayerCal.time}")
                    } catch (se: SecurityException) {
                        Log.w(TAG, "Exact alarm permission denied: ${se.message}")
                    } catch (t: Throwable) {
                        Log.e(TAG, "Error setting exact alarm for ${item.type.banglaName}", t)
                    }
                }

                if (!success) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                prayerCal.timeInMillis,
                                pendingIntent
                            )
                        } else {
                            alarmManager.set(
                                AlarmManager.RTC_WAKEUP,
                                prayerCal.timeInMillis,
                                pendingIntent
                            )
                        }
                        Log.d(TAG, "Inexact alarm set for ${item.type.banglaName} at ${prayerCal.time}")
                    } catch (t: Throwable) {
                        Log.e(TAG, "Error setting inexact alarm for ${item.type.banglaName}", t)
                    }
                }
            } else {
                // Notification disabled: cancel any existing pending alarm
                try {
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )
                    if (pendingIntent != null) {
                        alarmManager.cancel(pendingIntent)
                        pendingIntent.cancel()
                        Log.d(TAG, "Cancelled alarm for ${item.type.banglaName}")
                    }
                } catch (t: Throwable) {
                    Log.w(TAG, "Error cancelling alarm for ${item.type.banglaName}", t)
                }
            }
        }
    }

    /**
     * Backward-compatible delegation.
     */
    fun scheduleNextAzan(context: Context, items: List<PrayerTimeItem>, force: Boolean = false) {
        scheduleAllAzans(context, items)
    }

    /**
     * Cancels all scheduled prayer alarms.
     */
    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        for (type in PrayerType.values()) {
            val requestCode = getRequestCode(type)
            val intent = Intent(context, AzanAlarmReceiver::class.java).apply {
                action = AzanAlarmReceiver.ACTION_TRIGGER_AZAN
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}
