package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CityPreset
import com.example.data.model.PrayerTimeItem
import com.example.data.model.PrayerType
import com.example.ui.components.PrayerTimePickerDialog
import com.example.viewmodel.PrayerUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeCustomizationScreen(
    uiState: PrayerUiState,
    onSetCustomTime: (PrayerType, Int, Int) -> Unit,
    onSetJamatTime: (PrayerType, Int?, Int?) -> Unit,
    onAdjustOffset: (PrayerType, Int) -> Unit,
    onResetPrayer: (PrayerType) -> Unit,
    onResetAll: () -> Unit,
    onSelectCity: (CityPreset) -> Unit,
    onToggleAsrMethod: (Boolean) -> Unit,
    onToggleAzanPreview: () -> Unit,
    onToggleAudioAzanEnabled: (Boolean) -> Unit,
    onTestNotification: () -> Unit = {},
    onToggleNotification: (PrayerType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var editingTimePrayer by remember { mutableStateOf<PrayerTimeItem?>(null) }
    var editingJamatPrayer by remember { mutableStateOf<PrayerTimeItem?>(null) }
    var showResetAllDialog by remember { mutableStateOf(false) }
    var cityDropdownExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("customization_list"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // Info Banner
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "তথ্য",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "আজানের সময় পরিবর্তন ও সমন্বয়",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "নওদা জুম্মা মসজিদ বা আপনার স্থানীয় মসজিদের আজানের সঠিক সময় সরাসরি নির্বাচন করুন অথবা +/- মিনিট সমন্বয় করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // District & Asr Settings Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "সাধারণ সমন্বয় ও সেটিংস",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // District selector dropdown
                        ExposedDropdownMenuBox(
                            expanded = cityDropdownExpanded,
                            onExpandedChange = { cityDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = "${uiState.selectedCity.nameBn} (${uiState.selectedCity.nameEn})",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("জেলা / এলাকা") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "জেলা",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .testTag("city_dropdown")
                            )

                            ExposedDropdownMenu(
                                expanded = cityDropdownExpanded,
                                onDismissRequest = { cityDropdownExpanded = false }
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "📍 পশ্চিমবঙ্গ, ভারত (${CityPreset.westBengalDistricts.size}টি জেলা)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                                CityPreset.westBengalDistricts.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text("${city.nameBn} (${city.nameEn})") },
                                        onClick = {
                                            onSelectCity(city)
                                            cityDropdownExpanded = false
                                        }
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "📍 বাংলাদেশ",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                                CityPreset.bangladeshDistricts.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text("${city.nameBn} (${city.nameEn})") },
                                        onClick = {
                                            onSelectCity(city)
                                            cityDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hanafi vs Shafi'i Asr Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (uiState.isHanafiAsr) "আসরের মাযহাব: হানাফী (দ্বিগুণ ছায়া)" else "আসরের মাযহাব: শাফেয়ী / সাধারণ (একগুণ ছায়া)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (uiState.isHanafiAsr) "বাংলাদেশে প্রচলিত হানাফী নিয়ম অনুযায়ী আসর কিছুটা দেরিতে শুরু হয়।" else "শাফেয়ী, মালেকী ও হাম্বলী নিয়ম অনুযায়ী আসর জলদি শুরু হয়।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.isHanafiAsr,
                                onCheckedChange = { onToggleAsrMethod(it) },
                                modifier = Modifier.testTag("asr_method_switch")
                            )
                        }
                    }
                }
            }

            // Azan Audio Alert Settings Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("azan_audio_settings_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "আজানের শব্দ",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "আজানের অডিও ও অ্যালার্ট সেটিংস",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Toggle Azan audio switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "আজানের সময় আজান বাজবে",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "নামাজের ওয়াক্ত হলে স্বয়ংক্রিয়ভাবে মিষ্টি সুরে আজান পাঠ ও রিমাইন্ডার বাজবে।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = uiState.isAudioAzanEnabled,
                                onCheckedChange = { onToggleAudioAzanEnabled(it) },
                                modifier = Modifier.testTag("azan_audio_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Test / Preview button
                        Button(
                            onClick = onToggleAzanPreview,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isAzanPlaying) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_azan_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isAzanPlaying) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = "আজান পরীক্ষা",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isAzanPlaying) "আজান বন্ধ করুন (থামান)" else "আজান বাজিয়ে পরীক্ষা করুন 🔊",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Test Notification Alert Button
                        OutlinedButton(
                            onClick = onTestNotification,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_notification_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "নোটিফিকেশন অ্যালার্ট পরীক্ষা",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "নোটিফিকেশন অ্যালার্ট টেস্ট করুন 🔔",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Prayer Time Adjustment Cards
            items(uiState.prayerList, key = { it.type.id }) { item ->
                PrayerCustomizationCard(
                    item = item,
                    onOpenTimePicker = { editingTimePrayer = item },
                    onOpenJamatPicker = { editingJamatPrayer = item },
                    onClearJamat = { onSetJamatTime(item.type, null, null) },
                    onAdjustOffset = { delta -> onAdjustOffset(item.type, delta) },
                    onResetToDefault = { onResetPrayer(item.type) },
                    onToggleNotification = { onToggleNotification(item.type) }
                )
            }

            // Global Reset Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showResetAllDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_all_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "সকল সময় রিসেট",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "সকল সময় ডিফল্ট এ রিসেট করুন",
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Edit Prayer Waqt Time Dialog
        editingTimePrayer?.let { item ->
            PrayerTimePickerDialog(
                title = "${item.type.banglaName} আজানের সময় নির্ধারণ",
                subtitle = "সরাসরি ঘণ্টা ও মিনিট নির্বাচন করুন",
                initialHour = item.effectiveHour,
                initialMinute = item.effectiveMinute,
                onDismiss = { editingTimePrayer = null },
                onConfirm = { hour, minute ->
                    onSetCustomTime(item.type, hour, minute)
                    editingTimePrayer = null
                }
            )
        }

        // Edit Jamat Time Dialog
        editingJamatPrayer?.let { item ->
            PrayerTimePickerDialog(
                title = "${item.type.banglaName} জামাতের সময় নির্ধারণ",
                subtitle = "নওদা জুম্মা মসজিদ বা আপনার স্থানীয় মসজিদের জামাত সময় দিন",
                initialHour = item.jamatHour ?: item.effectiveHour,
                initialMinute = item.jamatMinute ?: ((item.effectiveMinute + 15) % 60),
                onDismiss = { editingJamatPrayer = null },
                onConfirm = { hour, minute ->
                    onSetJamatTime(item.type, hour, minute)
                    editingJamatPrayer = null
                }
            )
        }

        // Confirmation dialog for Reset All
        if (showResetAllDialog) {
            AlertDialog(
                onDismissRequest = { showResetAllDialog = false },
                title = { Text("সকল সময় রিসেট করবেন?") },
                text = { Text("আপনার করা সমস্ত আজান ও জামাতের কাস্টম সময় মুছে গিয়ে স্ট্যান্ডার্ড গণনায় ফিরে যাবে।") },
                confirmButton = {
                    Button(
                        onClick = {
                            onResetAll()
                            showResetAllDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("হ্যাঁ, রিসেট করুন")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetAllDialog = false }) {
                        Text("না")
                    }
                }
            )
        }
    }
}

@Composable
private fun PrayerCustomizationCard(
    item: PrayerTimeItem,
    onOpenTimePicker: () -> Unit,
    onOpenJamatPicker: () -> Unit,
    onClearJamat: () -> Unit,
    onAdjustOffset: (Int) -> Unit,
    onResetToDefault: () -> Unit,
    onToggleNotification: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            if (item.isCustomized) 2.dp else 1.dp,
            if (item.isCustomized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_card_${item.type.id.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Prayer Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.type.banglaName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${item.type.arabicName})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (item.isCustomized) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = "কাস্টমাইজড সময়",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current Time & Time Picker Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "আজানের সময়",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.timeFormattedBn,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ডিফল্ট গণনা: ${item.defaultTimeFormattedBn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Button(
                    onClick = onOpenTimePicker,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("set_exact_time_${item.type.id.lowercase()}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "সরাসরি সময় দিন",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("সময় দিন")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Micro-adjust Step Buttons (+/- 1m, 5m)
            Text(
                text = "দ্রুত মিনিট সমন্বয়:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(-5, -1, 1, 5).forEach { delta ->
                    OutlinedButton(
                        onClick = { onAdjustOffset(delta) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("step_${item.type.id.lowercase()}_$delta")
                    ) {
                        val sign = if (delta > 0) "+$delta" else "$delta"
                        Text(
                            text = "$sign মি",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prayer Notification & Azan Alert Switch
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (item.isNotificationEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (item.isNotificationEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = "ওয়াক্তের অ্যালার্ট",
                            tint = if (item.isNotificationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (item.isNotificationEnabled) "ওয়াক্তের আজান ও নোটিফিকেশন" else "আজান ও নোটিফিকেশন বন্ধ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (item.isNotificationEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = if (item.isNotificationEnabled) "সঠিক সময়ে আজান ও উচ্চ অগ্রাধিকার অ্যালার্ট বাজবে" else "এই ওয়াক্তে কোনো শব্দ বা অ্যালার্ট দেওয়া হবে না",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = item.isNotificationEnabled,
                        onCheckedChange = { onToggleNotification() },
                        modifier = Modifier.testTag("switch_notif_${item.type.id.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Jamat Time Section
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "জামাত",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "মসজিদের জামাত",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = item.jamatFormattedBn ?: "নির্ধারণ করা নেই",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (item.jamatFormattedBn != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Row {
                        TextButton(
                            onClick = onOpenJamatPicker,
                            modifier = Modifier.testTag("set_jamat_${item.type.id.lowercase()}")
                        ) {
                            Text("জামাত পরিবর্তন")
                        }

                        if (item.jamatHour != null) {
                            IconButton(
                                onClick = onClearJamat,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "জামাত মুছুন",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Reset this prayer to default
            if (item.isCustomized) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onResetToDefault,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("reset_prayer_${item.type.id.lowercase()}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "ডিফল্ট এ ফিরুন",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ডিফল্ট সময়ে ফিরুন")
                    }
                }
            }
        }
    }
}
