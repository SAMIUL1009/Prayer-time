package com.example.data.model

enum class PrayerType(
    val id: String,
    val banglaName: String,
    val englishName: String,
    val arabicName: String,
    val defaultHour: Int,
    val defaultMinute: Int,
    val defaultJamatHour: Int,
    val defaultJamatMinute: Int,
    val isFard: Boolean,
    val rakatsBn: String,
    val descriptionBn: String
) {
    FAJR(
        id = "FAJR",
        banglaName = "ফজর",
        englishName = "Fajr",
        arabicName = "الفجر",
        defaultHour = 4,
        defaultMinute = 30,
        defaultJamatHour = 5,
        defaultJamatMinute = 0,
        isFard = true,
        rakatsBn = "২ রাকাত সুন্নত, ২ রাকাত ফরজ",
        descriptionBn = "সুবহে সাদিক হতে সূর্যোদয়ের পূর্ব পর্যন্ত"
    ),
    SUNRISE(
        id = "SUNRISE",
        banglaName = "সূর্যোদয় / ইশরাক",
        englishName = "Sunrise",
        arabicName = "الشروق",
        defaultHour = 5,
        defaultMinute = 48,
        defaultJamatHour = 6,
        defaultJamatMinute = 15,
        isFard = false,
        rakatsBn = "২/৪ রাকাত নফল",
        descriptionBn = "সূর্যোদয় কালীন নামাজ পড়া নিষিদ্ধ, এর ২০ মিনিট পর ইশরাক শুরু"
    ),
    DHUHR(
        id = "DHUHR",
        banglaName = "যোহর",
        englishName = "Dhuhr",
        arabicName = "الظهر",
        defaultHour = 11,
        defaultMinute = 58,
        defaultJamatHour = 13,
        defaultJamatMinute = 15,
        isFard = true,
        rakatsBn = "৪ সুন্নত, ৪ ফরজ, ২ সুন্নত, ২ নফল",
        descriptionBn = "সূর্য মধ্যাকাশ হতে ঢলে পড়ার পর হতে আসর পূর্ব পর্যন্ত"
    ),
    ASR(
        id = "ASR",
        banglaName = "আসর",
        englishName = "Asr",
        arabicName = "العصر",
        defaultHour = 16,
        defaultMinute = 15,
        defaultJamatHour = 16,
        defaultJamatMinute = 45,
        isFard = true,
        rakatsBn = "৪ রাকাত ফরজ",
        descriptionBn = "কোনো বস্তুর ছায়া দ্বিগুণ হওয়া হতে সূর্যাস্তের পূর্ব পর্যন্ত"
    ),
    MAGHRIB(
        id = "MAGHRIB",
        banglaName = "মাগরিব",
        englishName = "Maghrib",
        arabicName = "المغرب",
        defaultHour = 17,
        defaultMinute = 55,
        defaultJamatHour = 18,
        defaultJamatMinute = 0,
        isFard = true,
        rakatsBn = "৩ ফরজ, ২ সুন্নত, ২ নফল",
        descriptionBn = "সূর্যাস্তের পর হতে পশ্চিমাকাশের লাল আভা মিলিয়ে যাওয়া পর্যন্ত"
    ),
    ISHA(
        id = "ISHA",
        banglaName = "এশা",
        englishName = "Isha",
        arabicName = "العشاء",
        defaultHour = 19,
        defaultMinute = 12,
        defaultJamatHour = 20,
        defaultJamatMinute = 0,
        isFard = true,
        rakatsBn = "৪ ফরজ, ২ সুন্নত, ৩ বিতর, ২ নফল",
        descriptionBn = "পশ্চিমাকাশের শুভ্রতা অদৃশ্য হওয়া হতে সুবহে সাদিকের পূর্ব পর্যন্ত"
    ),
    TAHAJJUD(
        id = "TAHAJJUD",
        banglaName = "তাহাজ্জুদ ও সাহরি",
        englishName = "Tahajjud / Sehri",
        arabicName = "التهجد",
        defaultHour = 3,
        defaultMinute = 30,
        defaultJamatHour = 4,
        defaultJamatMinute = 0,
        isFard = false,
        rakatsBn = "২/৪/৮/১২ রাকাত নফল",
        descriptionBn = "রাত্রির শেষ তৃতীয়াংশে তাহাজ্জুদ ও সাহরি সমাপ্তির উত্তম সময়"
    );

    companion object {
        fun fromId(id: String): PrayerType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: FAJR
        }

        val dailyFardPrayers: List<PrayerType>
            get() = listOf(FAJR, DHUHR, ASR, MAGHRIB, ISHA)

        val allSchedulePrayers: List<PrayerType>
            get() = listOf(FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA)
    }
}
