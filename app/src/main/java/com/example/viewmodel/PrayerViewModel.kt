package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.AzanPlayerManager
import com.example.data.local.DailyTrackerEntity
import com.example.data.local.PrayerDatabase
import com.example.data.local.PrayerEntity
import com.example.data.local.TasbihEntity
import com.example.data.model.CityPreset
import com.example.data.model.PrayerTimeItem
import com.example.data.model.PrayerType
import com.example.data.repository.PrayerRepository
import com.example.service.AzanNotificationHelper
import com.example.service.AzanScheduler
import com.example.util.IslamicDateUtil
import com.example.util.PrayerAlertPreferences
import com.example.util.PrayerCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class PrayerUiState(
    val prayerList: List<PrayerTimeItem> = emptyList(),
    val currentPrayer: PrayerTimeItem? = null,
    val nextPrayer: PrayerTimeItem? = null,
    val countdownTextBn: String = "০০:০০:০০",
    val countdownTextEn: String = "00:00:00",
    val progressFraction: Float = 0f,
    val sehriEndTimeBn: String = "",
    val iftarTimeBn: String = "",
    val prohibitedTimesBn: List<String> = emptyList(),
    val gregorianDateBn: String = "",
    val hijriDateBn: String = "",
    val selectedCity: CityPreset = CityPreset.westBengalDistricts.first(), // Default Nowda, Murshidabad
    val isHanafiAsr: Boolean = true,
    val completedCountToday: Int = 0,
    val totalFardCount: Int = 5,
    val isAnyCustomTimeActive: Boolean = false,
    val tasbihCount: Int = 0,
    val tasbihTarget: Int = 33,
    val tasbihTitle: String = "সুবহানাল্লাহ",
    val tasbihLaps: Int = 0,
    val isAzanPlaying: Boolean = false,
    val playingPrayerName: String? = null,
    val isAudioAzanEnabled: Boolean = true
)

private data class CalculationConfig(
    val city: CityPreset,
    val isHanafi: Boolean
)

class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PrayerRepository
    private val selectedCityFlow = MutableStateFlow(CityPreset.westBengalDistricts.first())
    private val isHanafiFlow = MutableStateFlow(true)
    private val isAudioAzanEnabledFlow = MutableStateFlow(PrayerAlertPreferences.isAudioAzanEnabled(application))
    private val currentDateKey = IslamicDateUtil.getTodayDateKey()
    private var lastTriggeredAzanTimeKey: String = ""

    private val configFlow = combine(selectedCityFlow, isHanafiFlow) { city, isHanafi ->
        CalculationConfig(city, isHanafi)
    }

    // Real-time ticking flow emitting every second
    private val clockFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000)
        }
    }

    init {
        val db = PrayerDatabase.getDatabase(application)
        repository = PrayerRepository(db.prayerDao())
        viewModelScope.launch {
            repository.ensureDefaultSettings()
        }
        AzanNotificationHelper.createNotificationChannel(application)
    }

    val uiState: StateFlow<PrayerUiState> = combine(
        repository.prayerSettingsFlow,
        repository.getRecordsForDate(currentDateKey),
        repository.tasbihFlow,
        configFlow,
        clockFlow
    ) { settingsList, trackerRecords, tasbihEntity, config, _ ->
        calculateUiState(
            settingsList = settingsList,
            trackerRecords = trackerRecords,
            tasbihEntity = tasbihEntity,
            city = config.city,
            isHanafi = config.isHanafi
        )
    }.combine(AzanPlayerManager.isPlaying) { state, isPlaying ->
        state.copy(isAzanPlaying = isPlaying)
    }.combine(AzanPlayerManager.currentPrayerName) { state, prayerName ->
        state.copy(playingPrayerName = prayerName)
    }.combine(isAudioAzanEnabledFlow) { state, isAudioEnabled ->
        state.copy(isAudioAzanEnabled = isAudioEnabled)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PrayerUiState()
    )

    init {
        viewModelScope.launch {
            uiState.map { it.prayerList }
                .distinctUntilChanged()
                .collect { prayerList ->
                    if (prayerList.isNotEmpty()) {
                        try {
                            AzanScheduler.scheduleNextAzan(application, prayerList)
                        } catch (_: Throwable) {
                        }
                    }
                }
        }
    }

    private fun calculateUiState(
        settingsList: List<PrayerEntity>,
        trackerRecords: List<DailyTrackerEntity>,
        tasbihEntity: TasbihEntity?,
        city: CityPreset,
        isHanafi: Boolean
    ): PrayerUiState {
        val cityTimeZone = TimeZone.getTimeZone(city.timeZoneId)
        val nowCal = Calendar.getInstance(cityTimeZone)

        // Exact astronomical calculation based on the city's latitude, longitude and local time zone
        val calculated = PrayerCalculator.calculateForDate(
            calendar = nowCal,
            lat = city.latitude,
            lng = city.longitude,
            timeZone = city.timeZone,
            isHanafi = isHanafi
        )

        val settingsMap = settingsList.associateBy { it.prayerId }
        val trackerMap = trackerRecords.associateBy { it.prayerId }

        fun getCityOffset(type: PrayerType): Int {
            return when (type) {
                PrayerType.FAJR -> city.fajrOffsetMin
                PrayerType.SUNRISE -> city.fajrOffsetMin
                PrayerType.DHUHR -> city.dhuhrOffsetMin
                PrayerType.ASR -> city.asrOffsetMin
                PrayerType.MAGHRIB -> city.maghribOffsetMin
                PrayerType.ISHA -> city.ishaOffsetMin
                PrayerType.TAHAJJUD -> city.fajrOffsetMin
            }
        }

        fun getAstronomicalDefaultMins(type: PrayerType): Int {
            return when (type) {
                PrayerType.FAJR -> calculated.fajrMinutes
                PrayerType.SUNRISE -> calculated.sunriseMinutes
                PrayerType.DHUHR -> calculated.dhuhrMinutes
                PrayerType.ASR -> calculated.asrMinutes
                PrayerType.MAGHRIB -> calculated.maghribMinutes
                PrayerType.ISHA -> calculated.ishaMinutes
                PrayerType.TAHAJJUD -> calculated.tahajjudMinutes
            }
        }

        var anyCustomActive = false

        val prayerItems = PrayerType.allSchedulePrayers.map { type ->
            val setting = settingsMap[type.id]
            val record = trackerMap[type.id]

            val baseMins = getAstronomicalDefaultMins(type) + getCityOffset(type)
            val defaultH = (baseMins / 60) % 24
            val defaultM = (baseMins % 60 + 60) % 60

            val customH = setting?.customHour
            val customM = setting?.customMinute
            val userOffset = setting?.offsetMinutes ?: 0

            if (customH != null || userOffset != 0) {
                anyCustomActive = true
            }

            val effectiveMins = when {
                customH != null && customM != null -> {
                    (customH * 60 + customM + userOffset + 1440) % 1440
                }
                else -> {
                    (baseMins + userOffset + 1440) % 1440
                }
            }

            val effectiveH = (effectiveMins / 60) % 24
            val effectiveM = (effectiveMins % 60 + 60) % 60

            PrayerTimeItem(
                type = type,
                effectiveHour = effectiveH,
                effectiveMinute = effectiveM,
                defaultHour = defaultH,
                defaultMinute = defaultM,
                customHour = customH,
                customMinute = customM,
                offsetMinutes = userOffset,
                jamatHour = setting?.jamatHour ?: type.defaultJamatHour,
                jamatMinute = setting?.jamatMinute ?: type.defaultJamatMinute,
                isNotificationEnabled = setting?.isNotificationEnabled ?: true,
                isCompletedToday = record?.isCompleted ?: false
            )
        }

        // Determine current and next prayer based on current time in city's time zone
        val currentNowMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
        val currentNowSeconds = nowCal.get(Calendar.SECOND)

        val fajrItem = prayerItems.first { it.type == PrayerType.FAJR }
        val sunriseItem = prayerItems.first { it.type == PrayerType.SUNRISE }
        val dhuhrItem = prayerItems.first { it.type == PrayerType.DHUHR }
        val asrItem = prayerItems.first { it.type == PrayerType.ASR }
        val maghribItem = prayerItems.first { it.type == PrayerType.MAGHRIB }
        val ishaItem = prayerItems.first { it.type == PrayerType.ISHA }

        val fajrM = fajrItem.effectiveHour * 60 + fajrItem.effectiveMinute
        val sunriseM = sunriseItem.effectiveHour * 60 + sunriseItem.effectiveMinute
        val dhuhrM = dhuhrItem.effectiveHour * 60 + dhuhrItem.effectiveMinute
        val asrM = asrItem.effectiveHour * 60 + asrItem.effectiveMinute
        val maghribM = maghribItem.effectiveHour * 60 + maghribItem.effectiveMinute
        val ishaM = ishaItem.effectiveHour * 60 + ishaItem.effectiveMinute

        var activeType = PrayerType.ISHA
        var nextType = PrayerType.FAJR
        var startWindowMins = ishaM
        var endWindowMins = fajrM + 1440

        when {
            currentNowMinutes in fajrM until sunriseM -> {
                activeType = PrayerType.FAJR
                nextType = PrayerType.SUNRISE
                startWindowMins = fajrM
                endWindowMins = sunriseM
            }
            currentNowMinutes in sunriseM until dhuhrM -> {
                activeType = PrayerType.SUNRISE
                nextType = PrayerType.DHUHR
                startWindowMins = sunriseM
                endWindowMins = dhuhrM
            }
            currentNowMinutes in dhuhrM until asrM -> {
                activeType = PrayerType.DHUHR
                nextType = PrayerType.ASR
                startWindowMins = dhuhrM
                endWindowMins = asrM
            }
            currentNowMinutes in asrM until maghribM -> {
                activeType = PrayerType.ASR
                nextType = PrayerType.MAGHRIB
                startWindowMins = asrM
                endWindowMins = maghribM
            }
            currentNowMinutes in maghribM until ishaM -> {
                activeType = PrayerType.MAGHRIB
                nextType = PrayerType.ISHA
                startWindowMins = maghribM
                endWindowMins = ishaM
            }
            else -> {
                activeType = PrayerType.ISHA
                nextType = PrayerType.FAJR
                startWindowMins = ishaM
                endWindowMins = if (currentNowMinutes >= ishaM) fajrM + 1440 else fajrM
            }
        }

        // Calculate countdown in seconds
        val nowTotalSeconds = currentNowMinutes * 60 + currentNowSeconds
        val nextTargetSeconds = if (endWindowMins > 1440 && nowTotalSeconds >= 1440) {
            endWindowMins * 60
        } else if (endWindowMins * 60 <= nowTotalSeconds) {
            (endWindowMins + 1440) * 60
        } else {
            endWindowMins * 60
        }

        val remainingSecs = kotlin.math.max(0, nextTargetSeconds - nowTotalSeconds)
        val hoursRemaining = remainingSecs / 3600
        val minsRemaining = (remainingSecs % 3600) / 60
        val secsRemaining = remainingSecs % 60

        // In-app Azan Trigger Check: exactly when Azan time arrives
        if (remainingSecs == 0 && isAudioAzanEnabledFlow.value) {
            val key = "${nowCal.get(Calendar.DAY_OF_YEAR)}_${nextType.id}"
            if (lastTriggeredAzanTimeKey != key && nextType != PrayerType.SUNRISE) {
                lastTriggeredAzanTimeKey = key
                AzanPlayerManager.playAzan(getApplication(), nextType.banglaName)
                AzanNotificationHelper.showAzanNotification(getApplication(), nextType.banglaName)
            }
        }

        val countdownBn = String.format(
            Locale.US,
            "%02d:%02d:%02d",
            hoursRemaining, minsRemaining, secsRemaining
        ).let { PrayerTimeItem.convertToBanglaDigits(it) }

        val countdownEn = String.format(
            Locale.US,
            "%02d:%02d:%02d",
            hoursRemaining, minsRemaining, secsRemaining
        )

        val totalWindowSecs = kotlin.math.max(1, (endWindowMins - startWindowMins) * 60)
        val elapsedSecs = kotlin.math.max(0, totalWindowSecs - remainingSecs)
        val fraction = (elapsedSecs.toFloat() / totalWindowSecs.toFloat()).coerceIn(0f, 1f)

        // Updated prayer items with current/next flags
        val updatedItems = prayerItems.map { item ->
            item.copy(
                isCurrent = item.type == activeType,
                isNext = item.type == nextType,
                isPassed = isTimePassed(item.effectiveHour, item.effectiveMinute, currentNowMinutes)
            )
        }

        val activeItem = updatedItems.firstOrNull { it.type == activeType }
        val nextItem = updatedItems.firstOrNull { it.type == nextType }

        // Sehri ends 3 mins before Fajr
        val sehriEndTotalMins = (fajrM - 3 + 1440) % 1440
        val sehriEndBn = PrayerTimeItem.formatTimeBn(sehriEndTotalMins / 60, sehriEndTotalMins % 60)
        val iftarBn = PrayerTimeItem.formatTimeBn(maghribItem.effectiveHour, maghribItem.effectiveMinute)

        val prohibitedTimes = listOf(
            "সূর্যোদয়: ${sunriseItem.timeFormattedBn} থেকে প্রায় ২০ মিনিট",
            "ঠিক দ্বিপ্রহর: ${PrayerTimeItem.formatTimeBn((dhuhrM - 10) / 60, (dhuhrM - 10) % 60)} থেকে যোহর শুরু পর্যন্ত",
            "সূর্যাস্তের মুহূর্ত: ${PrayerTimeItem.formatTimeBn((maghribM - 15) / 60, (maghribM - 15) % 60)} থেকে মাগরিব শুরু পর্যন্ত"
        )

        val completedFardCount = updatedItems.filter { it.type.isFard && it.isCompletedToday }.size

        return PrayerUiState(
            prayerList = updatedItems,
            currentPrayer = activeItem,
            nextPrayer = nextItem,
            countdownTextBn = countdownBn,
            countdownTextEn = countdownEn,
            progressFraction = fraction,
            sehriEndTimeBn = sehriEndBn,
            iftarTimeBn = iftarBn,
            prohibitedTimesBn = prohibitedTimes,
            gregorianDateBn = IslamicDateUtil.getFormattedGregorianBn(nowCal),
            hijriDateBn = IslamicDateUtil.getFormattedHijriBn(nowCal),
            selectedCity = city,
            isHanafiAsr = isHanafi,
            completedCountToday = completedFardCount,
            totalFardCount = 5,
            isAnyCustomTimeActive = anyCustomActive,
            tasbihCount = tasbihEntity?.currentCount ?: 0,
            tasbihTarget = tasbihEntity?.targetCount ?: 33,
            tasbihTitle = tasbihEntity?.title ?: "সুবহানাল্লাহ",
            tasbihLaps = tasbihEntity?.totalLaps ?: 0,
            isAzanPlaying = AzanPlayerManager.isPlaying.value,
            playingPrayerName = AzanPlayerManager.currentPrayerName.value,
            isAudioAzanEnabled = isAudioAzanEnabledFlow.value
        )
    }

    private fun isTimePassed(hour: Int, minute: Int, currentMinutes: Int): Boolean {
        return (hour * 60 + minute) < currentMinutes
    }

    // --- User Actions for Changing Times ---

    fun setCustomPrayerTime(type: PrayerType, hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.updateCustomTime(type.id, hour, minute)
        }
    }

    fun setJamatTime(type: PrayerType, hour: Int?, minute: Int?) {
        viewModelScope.launch {
            repository.updateJamatTime(type.id, hour, minute)
        }
    }

    fun adjustPrayerOffset(type: PrayerType, deltaMinutes: Int) {
        viewModelScope.launch {
            repository.adjustOffset(type.id, deltaMinutes)
        }
    }

    fun resetPrayerToDefault(type: PrayerType) {
        viewModelScope.launch {
            repository.resetPrayerToDefault(type.id)
        }
    }

    fun resetAllPrayersToDefault() {
        viewModelScope.launch {
            repository.resetAllToDefault()
        }
    }

    fun toggleNotification(type: PrayerType) {
        viewModelScope.launch {
            repository.toggleNotification(type.id)
        }
    }

    fun togglePrayerCompleted(type: PrayerType, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.togglePrayerCompleted(currentDateKey, type.id, !currentStatus)
        }
    }

    fun selectCity(city: CityPreset) {
        selectedCityFlow.value = city
    }

    fun setHanafiAsr(isHanafi: Boolean) {
        isHanafiFlow.value = isHanafi
    }

    // --- Azan Audio Controls ---

    fun toggleAzanPreview(prayerName: String = "ফজর") {
        AzanPlayerManager.togglePreview(getApplication(), prayerName)
    }

    fun stopAzanAudio() {
        AzanPlayerManager.stopAzan()
        AzanNotificationHelper.dismissNotification(getApplication())
    }

    fun toggleAudioAzanEnabled(enabled: Boolean) {
        PrayerAlertPreferences.setAudioAzanEnabled(getApplication(), enabled)
        isAudioAzanEnabledFlow.value = enabled
    }

    fun testNotificationAlert() {
        AzanNotificationHelper.triggerTestNotification(getApplication())
    }

    // --- Tasbih Actions ---

    fun incrementTasbih() {
        viewModelScope.launch {
            val current = uiState.value
            var nextCount = current.tasbihCount + 1
            var nextLaps = current.tasbihLaps
            if (nextCount >= current.tasbihTarget) {
                nextCount = 0
                nextLaps += 1
            }
            repository.saveTasbih(
                TasbihEntity(
                    currentCount = nextCount,
                    targetCount = current.tasbihTarget,
                    title = current.tasbihTitle,
                    totalLaps = nextLaps
                )
            )
        }
    }

    fun resetTasbih() {
        viewModelScope.launch {
            val current = uiState.value
            repository.saveTasbih(
                TasbihEntity(
                    currentCount = 0,
                    targetCount = current.tasbihTarget,
                    title = current.tasbihTitle,
                    totalLaps = 0
                )
            )
        }
    }

    fun setTasbihPreset(title: String, target: Int) {
        viewModelScope.launch {
            repository.saveTasbih(
                TasbihEntity(
                    currentCount = 0,
                    targetCount = target,
                    title = title,
                    totalLaps = 0
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        AzanPlayerManager.stopAzan()
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PrayerViewModel(application) as T
                }
            }
    }
}
