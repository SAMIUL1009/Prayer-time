package com.example.util

import com.example.data.model.PrayerType
import java.util.Calendar
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * Solar prayer times calculation based on astronomical formulas
 * Standard coordinates for Dhaka: Lat 23.8103, Lng 90.4125, TimeZone: +6.0
 */
object PrayerCalculator {

    data class CalculatedTimes(
        val fajrMinutes: Int,
        val sunriseMinutes: Int,
        val dhuhrMinutes: Int,
        val asrMinutes: Int,
        val sunsetMinutes: Int,
        val maghribMinutes: Int,
        val ishaMinutes: Int,
        val tahajjudMinutes: Int
    )

    fun calculateForDate(
        calendar: Calendar = Calendar.getInstance(),
        lat: Double = 23.8103,
        lng: Double = 90.4125,
        timeZone: Double = 6.0,
        isHanafi: Boolean = true
    ): CalculatedTimes {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val julianDay = getJulianDay(year, month, day)
        val d = julianDay - 2451545.0

        // Sun's mean anomaly
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        // Earth's obliquity
        val e = 23.439 - 0.00000036 * d

        // Sun's right ascension & declination
        val ra = Math.toDegrees(atan(cos(Math.toRadians(e)) * tan(Math.toRadians(l))))
        val adjustedRa = fixAngle(ra + (floor(l / 180.0) - floor(ra / 180.0)) * 180.0) / 15.0

        val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))

        // Equation of time
        val eqt = q / 15.0 - adjustedRa

        // Solar noon in hours
        val noon = 12.0 + timeZone - lng / 15.0 - eqt

        // Sun angles
        val fajrAngle = 18.0
        val ishaAngle = 18.0
        val sunriseZenith = 90.8333

        val fajrHour = noon - computeHourAngle(noon, fajrAngle, declination, lat)
        val sunriseHour = noon - computeSunAngle(sunriseZenith, declination, lat)
        val sunsetHour = noon + computeSunAngle(sunriseZenith, declination, lat)
        val maghribHour = sunsetHour + (2.0 / 60.0) // 2 mins safety buffer for Maghrib / Iftar
        val ishaHour = noon + computeHourAngle(noon, ishaAngle, declination, lat)

        // Asr calculation
        val asrFactor = if (isHanafi) 2.0 else 1.0
        val asrAngle = Math.toDegrees(atan(1.0 / (asrFactor + tan(Math.toRadians(Math.abs(lat - declination))))))
        val asrHour = noon + computeSunAngle(90.0 - asrAngle, declination, lat)

        // Dhuhr is noon + 2 min buffer
        val dhuhrHour = noon + (2.0 / 60.0)

        // Tahajjud: 1.5 hour before Fajr
        val fajrMins = toMinutes(fajrHour)
        val tahajjudMins = (fajrMins - 60 + 1440) % 1440

        return CalculatedTimes(
            fajrMinutes = fajrMins,
            sunriseMinutes = toMinutes(sunriseHour),
            dhuhrMinutes = toMinutes(dhuhrHour),
            asrMinutes = toMinutes(asrHour),
            sunsetMinutes = toMinutes(sunsetHour),
            maghribMinutes = toMinutes(maghribHour),
            ishaMinutes = toMinutes(ishaHour),
            tahajjudMinutes = tahajjudMins
        )
    }

    private fun computeSunAngle(angle: Double, declination: Double, lat: Double): Double {
        val cosH = (cos(Math.toRadians(angle)) - sin(Math.toRadians(lat)) * sin(Math.toRadians(declination))) /
                (cos(Math.toRadians(lat)) * cos(Math.toRadians(declination)))
        if (cosH > 1.0 || cosH < -1.0) return 0.0
        return Math.toDegrees(acos(cosH)) / 15.0
    }

    private fun computeHourAngle(noon: Double, angle: Double, declination: Double, lat: Double): Double {
        return computeSunAngle(90.0 + angle, declination, lat)
    }

    private fun toMinutes(hoursFraction: Double): Int {
        var h = hoursFraction
        while (h < 0) h += 24.0
        while (h >= 24) h -= 24.0
        return (h * 60.0 + 0.5).toInt()
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle - 360.0 * floor(angle / 360.0)
        if (a < 0) a += 360.0
        return a
    }

    private fun getJulianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }
}
