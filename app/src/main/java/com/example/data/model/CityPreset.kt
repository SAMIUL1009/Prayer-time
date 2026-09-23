package com.example.data.model

data class CityPreset(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val region: String, // "পশ্চিমবঙ্গ (ভারত)" or "বাংলাদেশ"
    val latitude: Double,
    val longitude: Double,
    val timeZone: Double, // 5.5 for India, 6.0 for Bangladesh
    val timeZoneId: String, // "Asia/Kolkata" or "Asia/Dhaka"
    val fajrOffsetMin: Int = 0,
    val dhuhrOffsetMin: Int = 0,
    val asrOffsetMin: Int = 0,
    val maghribOffsetMin: Int = 0,
    val ishaOffsetMin: Int = 0
) {
    companion object {
        const val REGION_WEST_BENGAL = "পশ্চিমবঙ্গ (ভারত)"
        const val REGION_BANGLADESH = "বাংলাদেশ"

        // All 23 districts of West Bengal, India (Nowda, Murshidabad first)
        val westBengalDistricts: List<CityPreset> = listOf(
            CityPreset("wb_murshidabad", "মুর্শিদাবাদ (নওদা / বহরমপুর)", "Murshidabad (Nowda)", REGION_WEST_BENGAL, 23.9000, 88.3500, 5.5, "Asia/Kolkata"),
            CityPreset("wb_kolkata", "কলকাতা", "Kolkata", REGION_WEST_BENGAL, 22.5726, 88.3639, 5.5, "Asia/Kolkata"),
            CityPreset("wb_north24", "উত্তর ২৪ পরগনা (বারাসাত)", "North 24 Parganas (Barasat)", REGION_WEST_BENGAL, 22.7230, 88.4800, 5.5, "Asia/Kolkata"),
            CityPreset("wb_south24", "দক্ষিণ ২৪ পরগনা (আলিপুর)", "South 24 Parganas (Alipore)", REGION_WEST_BENGAL, 22.5200, 88.3300, 5.5, "Asia/Kolkata"),
            CityPreset("wb_howrah", "হাওড়া", "Howrah", REGION_WEST_BENGAL, 22.5958, 88.2636, 5.5, "Asia/Kolkata"),
            CityPreset("wb_hooghly", "হুগলি (চুঁচুড়া)", "Hooghly (Chinsurah)", REGION_WEST_BENGAL, 22.9000, 88.3900, 5.5, "Asia/Kolkata"),
            CityPreset("wb_nadia", "নদিয়া (কৃষ্ণনগর)", "Nadia (Krishnanagar)", REGION_WEST_BENGAL, 23.4000, 88.5000, 5.5, "Asia/Kolkata"),
            CityPreset("wb_purba_bardhaman", "পূর্ব বর্ধমান", "Purba Bardhaman", REGION_WEST_BENGAL, 23.2324, 87.8615, 5.5, "Asia/Kolkata"),
            CityPreset("wb_paschim_bardhaman", "পশ্চিম বর্ধমান (আসানসোল)", "Paschim Bardhaman (Asansol)", REGION_WEST_BENGAL, 23.6889, 86.9661, 5.5, "Asia/Kolkata"),
            CityPreset("wb_birbhum", "বীরভূম (সিউড়ি)", "Birbhum (Suri)", REGION_WEST_BENGAL, 23.9054, 87.5246, 5.5, "Asia/Kolkata"),
            CityPreset("wb_bankura", "বাঁকুড়া", "Bankura", REGION_WEST_BENGAL, 23.2324, 87.0784, 5.5, "Asia/Kolkata"),
            CityPreset("wb_purulia", "পুরুলিয়া", "Purulia", REGION_WEST_BENGAL, 23.3322, 86.3652, 5.5, "Asia/Kolkata"),
            CityPreset("wb_purba_medinipur", "পূর্ব মেদিনীপুর (তমলুক)", "Purba Medinipur (Tamluk)", REGION_WEST_BENGAL, 22.2980, 87.9250, 5.5, "Asia/Kolkata"),
            CityPreset("wb_paschim_medinipur", "পশ্চিম মেদিনীপুর", "Paschim Medinipur", REGION_WEST_BENGAL, 22.4257, 87.3199, 5.5, "Asia/Kolkata"),
            CityPreset("wb_jhargram", "ঝাড়গ্রাম", "Jhargram", REGION_WEST_BENGAL, 22.4500, 86.9800, 5.5, "Asia/Kolkata"),
            CityPreset("wb_malda", "মালদা (ইংরেজবাজার)", "Malda (English Bazar)", REGION_WEST_BENGAL, 25.0000, 88.1400, 5.5, "Asia/Kolkata"),
            CityPreset("wb_uttar_dinajpur", "উত্তর দিনাজপুর (রায়গঞ্জ)", "Uttar Dinajpur (Raiganj)", REGION_WEST_BENGAL, 25.6200, 88.1200, 5.5, "Asia/Kolkata"),
            CityPreset("wb_dakshin_dinajpur", "দক্ষিণ দিনাজপুর (বালুরঘাট)", "Dakshin Dinajpur (Balurghat)", REGION_WEST_BENGAL, 25.2200, 88.7600, 5.5, "Asia/Kolkata"),
            CityPreset("wb_darjeeling", "দার্জিলিং", "Darjeeling", REGION_WEST_BENGAL, 27.0410, 88.2663, 5.5, "Asia/Kolkata"),
            CityPreset("wb_kalimpong", "কালিম্পং", "Kalimpong", REGION_WEST_BENGAL, 27.0667, 88.4667, 5.5, "Asia/Kolkata"),
            CityPreset("wb_jalpaiguri", "জলপাইগুড়ি", "Jalpaiguri", REGION_WEST_BENGAL, 26.5400, 88.7200, 5.5, "Asia/Kolkata"),
            CityPreset("wb_alipurduar", "আলিপুরদুয়ার", "Alipurduar", REGION_WEST_BENGAL, 26.4919, 89.5271, 5.5, "Asia/Kolkata"),
            CityPreset("wb_cooch_behar", "কোচবিহার", "Cooch Behar", REGION_WEST_BENGAL, 26.3236, 89.4497, 5.5, "Asia/Kolkata")
        )

        // Bangladesh Major Districts & Divisions
        val bangladeshDistricts: List<CityPreset> = listOf(
            CityPreset("dhaka", "ঢাকা", "Dhaka", REGION_BANGLADESH, 23.8103, 90.4125, 6.0, "Asia/Dhaka"),
            CityPreset("chittagong", "চট্টগ্রাম", "Chittagong", REGION_BANGLADESH, 22.3569, 91.7832, 6.0, "Asia/Dhaka", -5, -4, -4, -5, -5),
            CityPreset("sylhet", "সিলেট", "Sylhet", REGION_BANGLADESH, 24.8949, 91.8687, 6.0, "Asia/Dhaka", -6, -4, -4, -6, -6),
            CityPreset("rajshahi", "রাজশাহী", "Rajshahi", REGION_BANGLADESH, 24.3636, 88.6241, 6.0, "Asia/Dhaka", 6, 7, 7, 6, 6),
            CityPreset("khulna", "খুলনা", "Khulna", REGION_BANGLADESH, 22.8456, 89.5403, 6.0, "Asia/Dhaka", 4, 3, 3, 4, 4),
            CityPreset("barishal", "বরিশাল", "Barishal", REGION_BANGLADESH, 22.7010, 90.3535, 6.0, "Asia/Dhaka", 1, 1, 1, 1, 1),
            CityPreset("rangpur", "রংপুর", "Rangpur", REGION_BANGLADESH, 25.7439, 89.2752, 6.0, "Asia/Dhaka", 3, 6, 6, 4, 4),
            CityPreset("mymensingh", "ময়মনসিংহ", "Mymensingh", REGION_BANGLADESH, 24.7471, 90.4203, 6.0, "Asia/Dhaka", -1, 0, 0, -1, -1),
            CityPreset("bogura", "বগুড়া", "Bogura", REGION_BANGLADESH, 24.8465, 89.3777, 6.0, "Asia/Dhaka", 3, 4, 4, 3, 3),
            CityPreset("comilla", "কুমিল্লা", "Cumilla", REGION_BANGLADESH, 23.4607, 91.1809, 6.0, "Asia/Dhaka", -3, -2, -2, -3, -3),
            CityPreset("coxsbazar", "কক্সবাজার", "Cox's Bazar", REGION_BANGLADESH, 21.4272, 92.0058, 6.0, "Asia/Dhaka", -6, -5, -5, -6, -6)
        )

        val presets: List<CityPreset> = westBengalDistricts + bangladeshDistricts

        fun find(id: String): CityPreset = presets.firstOrNull { it.id == id } ?: westBengalDistricts.first()
    }
}
