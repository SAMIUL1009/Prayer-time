package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerTimeItem
import com.example.viewmodel.PrayerUiState

@Composable
fun TasbihScreen(
    uiState: PrayerUiState,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onSetPreset: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }

    fun triggerVibration() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(45)
            }
        } catch (_: Exception) {}
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(),
        label = "tasbihScale"
    )

    val progress = if (uiState.tasbihTarget > 0) {
        (uiState.tasbihCount.toFloat() / uiState.tasbihTarget.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val presets = listOf(
        Pair("সুবহানাল্লাহ (SubhanAllah)", 33),
        Pair("আলহামদুলিল্লাহ (Alhamdulillah)", 33),
        Pair("আল্লাহু আকবার (Allahu Akbar)", 34),
        Pair("আস্তাগফিরুল্লাহ (Astaghfirullah)", 100),
        Pair("লা ইলাহা ইল্লাল্লাহ", 100),
        Pair("দরূদ শরীফ", 100)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("tasbih_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Preset zikr chips
        Text(
            text = "যিকির নির্বাচন করুন:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { preset ->
                val isSelected = uiState.tasbihTitle.startsWith(preset.first.split(" ")[0])
                FilterChip(
                    selected = isSelected,
                    onClick = { onSetPreset(preset.first, preset.second) },
                    label = { Text("${preset.first.split(" ")[0]} (${preset.second})") }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Current Zikr Banner
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.tasbihTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                val targetBn = PrayerTimeItem.convertToBanglaDigits(uiState.tasbihTarget.toString())
                val lapsBn = PrayerTimeItem.convertToBanglaDigits(uiState.tasbihLaps.toString())
                Text(
                    text = "টার্গেট: $targetBn বার • সম্পন্ন চক্র: $lapsBn বার",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Big Tap Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(240.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        triggerVibration()
                        onIncrement()
                    }
                )
                .testTag("tasbih_tap_button")
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(230.dp),
                strokeWidth = 6.dp,
                color = Color(0xFFFFD54F),
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val countBn = PrayerTimeItem.convertToBanglaDigits(uiState.tasbihCount.toString())
                Text(
                    text = countBn,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 58.sp
                )
                Text(
                    text = "ট্যাপ করুন",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Reset Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.testTag("tasbih_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "রিসেট",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("গণনা ০ করুন")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
