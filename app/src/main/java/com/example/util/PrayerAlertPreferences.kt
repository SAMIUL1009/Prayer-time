package com.example.util

import android.content.Context
import android.content.SharedPreferences

object PrayerAlertPreferences {
    private const val PREF_NAME = "prayer_alert_prefs"
    private const val KEY_AUDIO_AZAN_ENABLED = "audio_azan_enabled"
    private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    private const val KEY_NOTIFICATION_SOUND_ENABLED = "notification_sound_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isAudioAzanEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUDIO_AZAN_ENABLED, true)
    }

    fun setAudioAzanEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUDIO_AZAN_ENABLED, enabled).apply()
    }

    fun isVibrationEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_VIBRATION_ENABLED, true)
    }

    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }

    fun isNotificationSoundEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_NOTIFICATION_SOUND_ENABLED, true)
    }

    fun setNotificationSoundEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_NOTIFICATION_SOUND_ENABLED, enabled).apply()
    }
}
