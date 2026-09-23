package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.audio.AzanPlayerManager
import com.example.util.PrayerAlertPreferences

object AzanNotificationHelper {
    const val CHANNEL_ID = "azan_prayer_alerts"
    const val NOTIFICATION_ID_BASE = 1000
    const val ACTION_STOP_AZAN = "com.example.action.STOP_AZAN"
    const val ACTION_MARK_PRAYED = "com.example.action.MARK_PRAYED"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_PRAYER_ID = "extra_prayer_id"

    private fun getAzanSoundUri(context: Context): Uri {
        val resId = AzanPlayerManager.getAzanRawResId(context)
        return if (resId != 0) {
            Uri.parse("android.resource://${context.packageName}/$resId")
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "আজান ও নামাজের সময়সূচী"
            val descriptionText = "নামাজের সঠিক সময়ে উচ্চ অগ্রাধিকারের আজান ও অ্যালার্ট"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val soundUri = getAzanSoundUri(context)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showAzanNotification(context: Context, prayerName: String, prayerId: String = "") {
        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_PRAYER_ID, prayerId)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            prayerId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop Azan Intent Action
        val stopIntent = Intent(context, AzanAlarmReceiver::class.java).apply {
            action = ACTION_STOP_AZAN
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_PRAYER_ID, prayerId)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            prayerId.hashCode() + 1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark as Prayed Intent Action
        val prayedIntent = Intent(context, AzanAlarmReceiver::class.java).apply {
            action = ACTION_MARK_PRAYED
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_PRAYER_ID, prayerId)
        }
        val prayedPendingIntent = PendingIntent.getBroadcast(
            context,
            prayerId.hashCode() + 2,
            prayedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = getAzanSoundUri(context)
        val isSoundEnabled = PrayerAlertPreferences.isNotificationSoundEnabled(context)
        val isVibrationEnabled = PrayerAlertPreferences.isVibrationEnabled(context)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🕌 $prayerName নামাজের ওয়াক্ত হয়েছে")
            .setContentText("নওদা জুম্মা মসজিদ • সঠিক সময় সালাত আদায় করুন")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🕌 $prayerName নামাজের ওয়াক্ত শুরু হয়েছে।\nনওদা জুম্মা মসজিদ • 'নিশ্চয়ই নামাজ মুমিনদের ওপর নির্দিষ্ট সময়ে ফরজ করা হয়েছে।' (সূরা আন-নিসা: ১০৩)")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "আজান বন্ধ করুন", stopPendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, "নামাজ পড়েছি ✓", prayedPendingIntent)

        if (isSoundEnabled) {
            notificationBuilder.setSound(soundUri)
        } else {
            notificationBuilder.setSilent(true)
        }

        if (isVibrationEnabled) {
            notificationBuilder.setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notifId = if (prayerId.isNotEmpty()) NOTIFICATION_ID_BASE + kotlin.math.abs(prayerId.hashCode() % 100) else NOTIFICATION_ID_BASE
        notificationManager.notify(notifId, notificationBuilder.build())
    }

    fun triggerTestNotification(context: Context) {
        showAzanNotification(context, "টেস্ট (ফজর)", "FAJR")
    }

    fun dismissNotification(context: Context, prayerId: String = "") {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (prayerId.isNotEmpty()) {
            val notifId = NOTIFICATION_ID_BASE + kotlin.math.abs(prayerId.hashCode() % 100)
            notificationManager.cancel(notifId)
        }
        notificationManager.cancel(NOTIFICATION_ID_BASE)
    }
}
