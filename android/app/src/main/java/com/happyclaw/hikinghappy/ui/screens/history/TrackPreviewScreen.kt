package com.happyclaw.hikinghappy.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyclaw.hikinghappy.ui.components.AmapView
import com.happyclaw.hikinghappy.ui.theme.HHColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrackPreviewScreen(
    viewModel: TrackPreviewViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HHColors.Background)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HHColors.Surface)
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = HHColors.TextPrimary
                )
            }
            Text(
                text = state.session?.let {
                    val loc = it.location?.takeIf { l -> l.isNotBlank() }
                    if (loc != null) "${it.activityType.displayName()} - $loc"
                    else it.activityType.displayName()
                } ?: "Track Preview",
                style = MaterialTheme.typography.headlineLarge,
                color = HHColors.TextPrimary,
                maxLines = 1
            )
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading...", color = HHColors.TextSecondary)
            }
        } else if (state.points.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No track data", color = HHColors.TextSecondary, fontSize = 14.sp)
            }
        } else {
            // Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AmapView(
                    latitude = state.centerLat,
                    longitude = state.centerLon,
                    hasFix = state.centerLat != 0.0,
                    trackPoints = state.points,
                    fitTrack = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Stats bar at bottom
            StatsBar(session = state.session)
        }
    }
}

@Composable
private fun StatsBar(session: com.happyclaw.hikinghappy.data.local.entity.TrackSession?) {
    if (session == null) return

    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    val startTime = dateFormat.format(Date(session.startTime))
    val endTime = session.endTime?.let { dateFormat.format(Date(it)) } ?: "--"

    val distanceStr = if (session.totalDistance >= 1000) {
        "%.2f km".format(session.totalDistance / 1000.0)
    } else {
        "%.0f m".format(session.totalDistance)
    }

    val durationStr = formatDuration(session.totalDuration)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HHColors.Surface)
    ) {
        HorizontalDivider(color = HHColors.BorderSubtle, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
        ) {
            StatItem(label = "Start", value = startTime)
            StatItem(label = "End", value = endTime)
            StatItem(label = "Distance", value = distanceStr)
            StatItem(label = "Duration", value = durationStr)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = HHColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = HHColors.TextTertiary
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return if (h > 0) "${h}h ${String.format("%02d", m)}m"
    else "${m}m"
}
