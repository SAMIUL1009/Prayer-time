package com.example

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PrayerTimeItem
import com.example.data.model.PrayerType
import com.example.service.AzanNotificationHelper
import com.example.service.AzanScheduler
import com.example.util.PrayerAlertPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Namaj Somoy", appName)
  }

  @Test
  fun `notification channel and alert creation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    AzanNotificationHelper.createNotificationChannel(context)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = notificationManager.getNotificationChannel(AzanNotificationHelper.CHANNEL_ID)
    assertNotNull(channel)
    assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)

    // Trigger test notification
    AzanNotificationHelper.triggerTestNotification(context)
    val notifications = notificationManager.activeNotifications
    assertTrue(notifications.isNotEmpty())
  }

  @Test
  fun `prayer alert preferences persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    PrayerAlertPreferences.setAudioAzanEnabled(context, false)
    assertEquals(false, PrayerAlertPreferences.isAudioAzanEnabled(context))

    PrayerAlertPreferences.setAudioAzanEnabled(context, true)
    assertEquals(true, PrayerAlertPreferences.isAudioAzanEnabled(context))
  }

  @Test
  fun `azan scheduler sets up alarms without crashing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val items = listOf(
      PrayerTimeItem(
        type = PrayerType.FAJR,
        effectiveHour = 4,
        effectiveMinute = 30,
        defaultHour = 4,
        defaultMinute = 30,
        customHour = null,
        customMinute = null,
        offsetMinutes = 0,
        jamatHour = 5,
        jamatMinute = 0,
        isNotificationEnabled = true,
        isCompletedToday = false
      )
    )
    AzanScheduler.scheduleAllAzans(context, items)
    AzanScheduler.cancelAllAlarms(context)
  }
}
