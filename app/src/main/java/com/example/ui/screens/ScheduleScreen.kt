package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.graphics.Color
import com.example.data.model.CityPreset
import com.example.data.model.PrayerTimeItem
import com.example.data.model.PrayerType
import com.example.ui.components.NextPrayerHeader
import com.example.ui.components.PrayerCard
import com.example.ui.components.PrayerTimePickerDialog
import com.example.ui.components.ProhibitedTimeCard
import com.example.ui.components.SehriIftarCard
import com.example.viewmodel.PrayerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    uiState: PrayerUiState,
    onNavigateToCustomTimes: () -> Unit,
    onSetCustomTime: (PrayerType, Int, Int) -> Unit,
    onToggleNotification: (PrayerType) -> Unit,
    onToggleCompleted: (PrayerType, Boolean) -> Unit,
    onSelectCity: (CityPreset) -> Unit,
    onToggleAzanPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingPrayer by remember { mutableStateOf<PrayerTimeItem?>(null) }
    var showCitySheet by remember { mutableStateOf(false) }
    var citySearchQuery by remember { mutableStateOf("") }
    var selectedRegionTab by remember { mutableIntStateOf(0) } // 0 = West Bengal, 1 = Bangladesh

    val filteredCities = remember(citySearchQuery, selectedRegionTab) {
        if (citySearchQuery.isNotBlank()) {
            CityPreset.presets.filter {
                it.nameBn.contains(citySearchQuery.trim(), ignoreCase = true) ||
                it.nameEn.contains(citySearchQuery.trim(), ignoreCase = true)
            }
        } else {
            if (selectedRegionTab == 0) {
                CityPreset.westBengalDistricts
            } else {
                CityPreset.bangladeshDistricts
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("schedule_list"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                NextPrayerHeader(
                    uiState = uiState,
                    onNavigateToCustomTimes = onNavigateToCustomTimes,
                    onOpenCitySelector = {
                        citySearchQuery = ""
                        selectedRegionTab = if (uiState.selectedCity.region == CityPreset.REGION_WEST_BENGAL) 0 else 1
                        showCitySheet = true
                    }
                )
            }

            item {
                SehriIftarCard(
                    sehriTimeBn = uiState.sehriEndTimeBn,
                    iftarTimeBn = uiState.iftarTimeBn
                )
            }

            // Quick action buttons: Change time & Test Azan audio
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToCustomTimes,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_change_times_banner")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "আজান ও জামাত সময় পরিবর্তন",
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = "আজানের সময়",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onToggleAzanPreview,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isAzanPlaying) Color(0xFFD32F2F) else MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = if (uiState.isAzanPlaying) Color.White else MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preview_azan_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isAzanPlaying) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = "আজান শুনুন",
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = if (uiState.isAzanPlaying) "আজান থামান" else "আজান শুনুন 🔊",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Prayer cards list
            items(uiState.prayerList, key = { it.type.id }) { item ->
                PrayerCard(
                    item = item,
                    onEditTime = { editingPrayer = item },
                    onToggleNotification = { onToggleNotification(item.type) },
                    onToggleCompleted = { onToggleCompleted(item.type, item.isCompletedToday) }
                )
            }

            // Prohibited times caution
            item {
                ProhibitedTimeCard(prohibitedTimes = uiState.prohibitedTimesBn)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Quick Edit Time Picker Dialog
        editingPrayer?.let { item ->
            PrayerTimePickerDialog(
                title = "${item.type.banglaName} আজানের সময় নির্ধারণ",
                subtitle = "নওদা জুম্মা মসজিদ বা আপনার স্থানীয় মসজিদের আজানের সময় সেট করুন",
                initialHour = item.effectiveHour,
                initialMinute = item.effectiveMinute,
                onDismiss = { editingPrayer = null },
                onConfirm = { hour, minute ->
                    onSetCustomTime(item.type, hour, minute)
                    editingPrayer = null
                }
            )
        }

        // City / District selection bottom sheet
        if (showCitySheet) {
            ModalBottomSheet(
                onDismissRequest = { showCitySheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "জেলা / এলাকা নির্বাচন করুন",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "ভারতের পশ্চিমবঙ্গের ২৩টি জেলা ও বাংলাদেশের জেলার নির্ভুল সময়সূচী।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Region Tabs
                    TabRow(
                        selectedTabIndex = selectedRegionTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Tab(
                            selected = selectedRegionTab == 0,
                            onClick = { selectedRegionTab = 0 },
                            text = { Text("পশ্চিমবঙ্গ (ভারত)", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedRegionTab == 1,
                            onClick = { selectedRegionTab = 1 },
                            text = { Text("বাংলাদেশ", fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search box
                    OutlinedTextField(
                        value = citySearchQuery,
                        onValueChange = { citySearchQuery = it },
                        placeholder = { Text("জেলার নাম খুঁজুন (উদা: মুর্শিদাবাদ, হাওড়া...)") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "অনুসন্ধান") },
                        trailingIcon = {
                            if (citySearchQuery.isNotEmpty()) {
                                IconButton(onClick = { citySearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "মুছুন")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_district_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                    ) {
                        items(filteredCities, key = { it.id }) { city ->
                            val isSelected = city.id == uiState.selectedCity.id
                            Surface(
                                onClick = {
                                    onSelectCity(city)
                                    showCitySheet = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                shape = MaterialTheme.shapes.medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = city.nameBn,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Text(
                                            text = "${city.nameEn} • ${city.region}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            onSelectCity(city)
                                            showCitySheet = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
