package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AzanPlayingBanner
import com.example.ui.screens.QiblaDuaScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.TasbihScreen
import com.example.ui.screens.TimeCustomizationScreen
import com.example.ui.screens.TrackerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PrayerViewModel

sealed class AppTab(
    val index: Int,
    val titleBn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Schedule : AppTab(0, "সময়সূচী", Icons.Filled.AccessTime, Icons.Outlined.AccessTime)
    object CustomTime : AppTab(1, "আজানের সময়", Icons.Filled.Tune, Icons.Outlined.Tune)
    object Tracker : AppTab(2, "ট্র্যাকার", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircleOutline)
    object Tasbih : AppTab(3, "তাসবীহ", Icons.Filled.Fingerprint, Icons.Outlined.Fingerprint)
    object QiblaDua : AppTab(4, "দোয়া ও কিবলা", Icons.Filled.Explore, Icons.Outlined.Explore)

    companion object {
        val allTabs = listOf(Schedule, CustomTime, Tracker, Tasbih, QiblaDua)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val prayerViewModel: PrayerViewModel = viewModel(
                    factory = PrayerViewModel.provideFactory(application)
                )

                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ -> }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                MainScreen(viewModel = prayerViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: PrayerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Mosque,
                                    contentDescription = "নওদা জুম্মা মসজিদ লোগো",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "নওদা জুম্মা মসজিদ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "আজান ও নামাজের সময়সূচী • ${uiState.selectedCity.nameBn}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.isAnyCustomTimeActive) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = "কাস্টম সময়",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                AppTab.allTabs.forEach { tab ->
                    val isSelected = selectedTabIndex == tab.index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = tab.index },
                        icon = {
                            if (tab == AppTab.CustomTime && uiState.isAnyCustomTimeActive) {
                                BadgedBox(badge = { Badge { Text("✓") } }) {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.titleBn,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.titleBn,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = tab.titleBn,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.index}")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // In-app Azan playing banner
            AzanPlayingBanner(
                isPlaying = uiState.isAzanPlaying,
                prayerName = uiState.playingPrayerName,
                onStopAzan = { viewModel.stopAzanAudio() }
            )

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screenTabTransition"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> ScheduleScreen(
                            uiState = uiState,
                            onNavigateToCustomTimes = { selectedTabIndex = 1 },
                            onSetCustomTime = { type, hour, minute ->
                                viewModel.setCustomPrayerTime(type, hour, minute)
                            },
                            onToggleNotification = { type ->
                                viewModel.toggleNotification(type)
                            },
                            onToggleCompleted = { type, status ->
                                viewModel.togglePrayerCompleted(type, status)
                            },
                            onSelectCity = { city ->
                                viewModel.selectCity(city)
                            },
                            onToggleAzanPreview = {
                                viewModel.toggleAzanPreview()
                            }
                        )
                        1 -> TimeCustomizationScreen(
                            uiState = uiState,
                            onSetCustomTime = { type, hour, minute ->
                                viewModel.setCustomPrayerTime(type, hour, minute)
                            },
                            onSetJamatTime = { type, hour, minute ->
                                viewModel.setJamatTime(type, hour, minute)
                            },
                            onAdjustOffset = { type, delta ->
                                viewModel.adjustPrayerOffset(type, delta)
                            },
                            onResetPrayer = { type ->
                                viewModel.resetPrayerToDefault(type)
                            },
                            onResetAll = {
                                viewModel.resetAllPrayersToDefault()
                            },
                            onSelectCity = { city ->
                                viewModel.selectCity(city)
                            },
                            onToggleAsrMethod = { isHanafi ->
                                viewModel.setHanafiAsr(isHanafi)
                            },
                            onToggleAzanPreview = {
                                viewModel.toggleAzanPreview()
                            },
                            onToggleAudioAzanEnabled = { isAudioEnabled ->
                                viewModel.toggleAudioAzanEnabled(isAudioEnabled)
                            },
                            onTestNotification = {
                                viewModel.testNotificationAlert()
                            },
                            onToggleNotification = { type ->
                                viewModel.toggleNotification(type)
                            }
                        )
                        2 -> TrackerScreen(
                            uiState = uiState,
                            onToggleCompleted = { type, status ->
                                viewModel.togglePrayerCompleted(type, status)
                            }
                        )
                        3 -> TasbihScreen(
                            uiState = uiState,
                            onIncrement = { viewModel.incrementTasbih() },
                            onReset = { viewModel.resetTasbih() },
                            onSetPreset = { title, target ->
                                viewModel.setTasbihPreset(title, target)
                            }
                        )
                        4 -> QiblaDuaScreen()
                    }
                }
            }
        }
    }
}
