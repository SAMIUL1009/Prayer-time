package com.example.util

import com.example.data.model.PrayerTimeItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object IslamicDateUtil {

    private val hijriMonthsBn = listOf(
        "মুহররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
        "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শা'বান",
        "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )

    private val daysBn = mapOf(
        Calendar.SUNDAY to "রবিবার",
        Calendar.MONDAY to "সোমবার",
        Calendar.TUESDAY to "মঙ্গলবার",
        Calendar.WEDNESDAY to "বুধবার",
        Calendar.THURSDAY to "বৃহস্পতিবার",
        Calendar.FRIDAY to "শুক্রবার",
        Calendar.SATURDAY to "শনিবার"
    )

    private val monthsBn = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    fun getFormattedGregorianBn(calendar: Calendar = Calendar.getInstance()): String {
        val dayName = daysBn[calendar.get(Calendar.DAY_OF_WEEK)] ?: ""
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val monthName = monthsBn[calendar.get(Calendar.MONTH)]
        val year = calendar.get(Calendar.YEAR)

        val dayBn = PrayerTimeItem.convertToBanglaDigits(day.toString())
        val yearBn = PrayerTimeItem.convertToBanglaDigits(year.toString())

        return "$dayName, $dayBn $monthName $yearBn"
    }

    /**
     * Approximate Islamic Hijri date calculation based on Kuwaiti algorithm
     */
    fun getFormattedHijriBn(calendar: Calendar = Calendar.getInstance()): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) // 0-indexed
        val year = calendar.get(Calendar.YEAR)

        var m = month + 1
        var y = year
        if (m < 3) {
            y -= 1
            m += 12
        }

        val a = kotlin.math.floor(y / 100.0)
        val b = 2 - a + kotlin.math.floor(a / 4.0)
        val jd = kotlin.math.floor(365.25 * (y + 4716)) + kotlin.math.floor(30.6001 * (m + 1)) + day + b - 1524

        val epoch = 1948439.5
        val daysSinceEpoch = jd - epoch
        val cycle = kotlin.math.floor(daysSinceEpoch / 10631.0)
        val remaining = daysSinceEpoch - cycle * 10631.0
        val yearInCycle = kotlin.math.floor((remaining + 0.5) / 354.366)
        val hijriYear = (cycle * 30 + yearInCycle + 1).toInt()

        val dayOfYear = remaining - kotlin.math.floor(yearInCycle * 354.366)
        val hijriMonthIndex = kotlin.math.min(11, kotlin.math.max(0, kotlin.math.floor((dayOfYear + 0.5) / 29.5).toInt()))
        val hijriDay = kotlin.math.max(1, (dayOfYear - kotlin.math.floor(hijriMonthIndex * 29.5) + 1).toInt())

        val dayBn = PrayerTimeItem.convertToBanglaDigits(hijriDay.toString())
        val yearBn = PrayerTimeItem.convertToBanglaDigits(hijriYear.toString())
        val monthName = hijriMonthsBn.getOrElse(hijriMonthIndex) { "হিজরী" }

        return "$dayBn $monthName, $yearBn হিজরী"
    }

    fun getTodayDateKey(calendar: Calendar = Calendar.getInstance()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(calendar.time)
    }
}
