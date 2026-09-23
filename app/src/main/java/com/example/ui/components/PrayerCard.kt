package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerTimeItem

@Composable
fun PrayerCard(
    item: PrayerTimeItem,
    onEditTime: () -> Unit,
    onToggleNotification: () -> Unit,
    onToggleCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrent = item.isCurrent
    val isNext = item.isNext

    val borderColor by animateColorAsState(
        targetValue = when {
            isCurrent -> MaterialTheme.colorScheme.primary
            isNext -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        },
        label = "cardBorderColor"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            isCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            item.isCompletedToday -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else -> MaterialTheme.colorScheme.surface
        },
        label = "cardContainerColor"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(if (isCurrent || isNext) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 4.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("prayer_card_${item.type.id.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Top Row: Names, Status Badges & Quick Action
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
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.type.arabicName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = "চলমান",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (isNext) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = "পরবর্তী",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Custom time badge indicator
                if (item.isCustomized) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        val offsetLabel = if (item.offsetMinutes > 0) {
                            "+${PrayerTimeItem.convertToBanglaDigits(item.offsetMinutes.toString())} মি"
                        } else if (item.offsetMinutes < 0) {
                            "${PrayerTimeItem.convertToBanglaDigits(item.offsetMinutes.toString())} মি"
                        } else {
                            "কাস্টম"
                        }
                        Text(
                            text = offsetLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Timings Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Azan Time
                Column {
                    Text(
                        text = "আজানের সময়",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.timeFormattedBn,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    if (item.customHour != null || item.offsetMinutes != 0) {
                        Text(
                            text = "ডিফল্ট আজান: ${item.defaultTimeFormattedBn}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Jamat Time (if configured)
                if (item.jamatFormattedBn != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = "জামাত",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "জামাত",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = item.jamatFormattedBn ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Action Buttons: Edit Time & Notification & Tracker Check
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Edit Time Button (✏️)
                    IconButton(
                        onClick = onEditTime,
                        modifier = Modifier.testTag("edit_time_${item.type.id.lowercase()}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "সময় পরিবর্তন করুন",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Notification toggle
                    IconButton(
                        onClick = onToggleNotification,
                        modifier = Modifier.testTag("notif_toggle_${item.type.id.lowercase()}")
                    ) {
                        Icon(
                            imageVector = if (item.isNotificationEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                            contentDescription = "বিজ্ঞপ্তি পরিবর্তন",
                            tint = if (item.isNotificationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Prayer Completed Checkbox (for Fard prayers)
                    if (item.type.isFard) {
                        IconButton(
                            onClick = onToggleCompleted,
                            modifier = Modifier.testTag("check_prayer_${item.type.id.lowercase()}")
                        ) {
                            Icon(
                                imageVector = if (item.isCompletedToday) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                contentDescription = "নামাজ সম্পন্ন",
                                tint = if (item.isCompletedToday) Color(0xFF00A86B) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
