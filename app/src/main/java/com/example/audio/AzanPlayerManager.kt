package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AzanPlayerManager {
    private const val TAG = "AzanPlayerManager"

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPrayerName = MutableStateFlow<String?>(null)
    val currentPrayerName: StateFlow<String?> = _currentPrayerName.asStateFlow()

    fun getAzanRawResId(context: Context): Int {
        // Direct reference first
        try {
            val directId = com.example.R.raw.azan
            if (directId != 0) return directId
        } catch (_: Throwable) {}

        // Identifier lookup fallback by namespace and by applicationId
        val idByExample = context.resources.getIdentifier("azan", "raw", "com.example")
        if (idByExample != 0) return idByExample

        return context.resources.getIdentifier("azan", "raw", context.packageName)
    }

    fun playAzan(context: Context, prayerName: String, isPreview: Boolean = false) {
        stopAzan()

        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NowdaApp:AzanWakeLock")?.apply {
                acquire(5 * 60 * 1000L) // 5 minutes safety timeout
            }

            // Ensure stream volume is audible
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                if (audioManager != null) {
                    val streamType = if (isPreview) AudioManager.STREAM_MUSIC else AudioManager.STREAM_ALARM
                    val currentVol = audioManager.getStreamVolume(streamType)
                    val maxVol = audioManager.getStreamMaxVolume(streamType)
                    if (currentVol <= 0) {
                        audioManager.setStreamVolume(streamType, (maxVol * 0.85).toInt().coerceAtLeast(1), 0)
                    }

                    val musicVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    val maxMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    if (musicVol <= 0) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (maxMusic * 0.85).toInt().coerceAtLeast(1), 0)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not adjust audio manager volume", e)
            }

            val resId = getAzanRawResId(context)
            if (resId == 0) {
                Log.e(TAG, "Azan raw resource 'azan' not found in package ${context.packageName}")
                if (isPreview) {
                    Toast.makeText(context, "আজান অডিও ফাইল লোড করা যায়নি", Toast.LENGTH_SHORT).show()
                }
                return
            }

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(if (isPreview) AudioAttributes.USAGE_MEDIA else AudioAttributes.USAGE_ALARM)
                .build()

            val player = try {
                MediaPlayer.create(context, resId, audioAttributes, 0)
            } catch (_: Exception) {
                null
            } ?: MediaPlayer.create(context, resId)

            if (player != null) {
                mediaPlayer = player.apply {
                    setVolume(1.0f, 1.0f)
                    setOnCompletionListener {
                        stopAzan()
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                        stopAzan()
                        true
                    }
                    start()
                }

                _isPlaying.value = true
                _currentPrayerName.value = prayerName
                Log.d(TAG, "Azan playing started successfully for $prayerName (isPreview=$isPreview)")

                if (isPreview) {
                    try {
                        Toast.makeText(
                            context,
                            "🔊 আজান বাজছে... সাউন্ড শুনতে না পেলে ডিভাইসের ভলিউম বাড়ান",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (_: Exception) {}
                }
            } else {
                Log.e(TAG, "MediaPlayer.create returned null for resId=$resId")
                _isPlaying.value = false
                _currentPrayerName.value = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Azan playback", e)
            stopAzan()
        }
    }

    fun stopAzan() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing media player", e)
        } finally {
            mediaPlayer = null
        }

        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing wake lock", e)
        } finally {
            wakeLock = null
        }

        _isPlaying.value = false
        _currentPrayerName.value = null
    }

    fun togglePreview(context: Context, prayerName: String = "ফজর") {
        if (_isPlaying.value) {
            stopAzan()
            try {
                Toast.makeText(context, "আজান বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {}
        } else {
            playAzan(context, prayerName, isPreview = true)
        }
    }
}
