package com.example.data.model

import java.util.Locale

data class PrayerTimeItem(
    val type: PrayerType,
    val effectiveHour: Int,
    val effectiveMinute: Int,
    val defaultHour: Int,
    val defaultMinute: Int,
    val customHour: Int? = null,
    val customMinute: Int? = null,
    val offsetMinutes: Int = 0,
    val jamatHour: Int? = null,
    val jamatMinute: Int? = null,
    val isNotificationEnabled: Boolean = true,
    val isCompletedToday: Boolean = false,
    val isCurrent: Boolean = false,
    val isNext: Boolean = false,
    val isPassed: Boolean = false
) {
    val isCustomized: Boolean
        get() = (customHour != null && customMinute != null) || offsetMinutes != 0

    val timeFormattedBn: String
        get() = formatTimeBn(effectiveHour, effectiveMinute)

    val timeFormattedEn: String
        get() = formatTimeEn(effectiveHour, effectiveMinute)

    val defaultTimeFormattedBn: String
        get() = formatTimeBn(defaultHour, defaultMinute)

    val jamatFormattedBn: String?
        get() = if (jamatHour != null && jamatMinute != null) {
            formatTimeBn(jamatHour, jamatMinute)
        } else null

    companion object {
        fun formatTimeBn(hour24: Int, minute: Int): String {
            val period = if (hour24 < 12) "ভোর" else if (hour24 == 12) "দুপুর" else if (hour24 < 16) "দুপুর" else if (hour24 < 18) "বিকাল" else if (hour24 < 20) "সন্ধ্যা" else "রাত"
            val hour12 = when {
                hour24 == 0 -> 12
                hour24 > 12 -> hour24 - 12
                else -> hour24
            }
            val hourBn = convertToBanglaDigits(String.format(Locale.US, "%02d", hour12))
            val minBn = convertToBanglaDigits(String.format(Locale.US, "%02d", minute))
            return "$period $hourBn:$minBn"
        }

        fun formatTimeEn(hour24: Int, minute: Int): String {
            val amPm = if (hour24 < 12) "AM" else "PM"
            val hour12 = when {
                hour24 == 0 -> 12
                hour24 > 12 -> hour24 - 12
                else -> hour24
            }
            return String.format(Locale.US, "%02d:%02d %s", hour12, minute, amPm)
        }

        fun convertToBanglaDigits(input: String): String {
            val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
            val sb = java.lang.StringBuilder()
            for (ch in input) {
                if (ch in '0'..'9') {
                    sb.append(bnDigits[ch - '0'])
                } else {
                    sb.append(ch)
                }
            }
            return sb.toString()
        }
    }
}
