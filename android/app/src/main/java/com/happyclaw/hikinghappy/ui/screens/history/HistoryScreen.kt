package com.happyclaw.hikinghappy.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyclaw.hikinghappy.data.local.entity.TrackSession
import com.happyclaw.hikinghappy.ui.theme.HHColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onTrackClick: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show export messages via snackbar
    LaunchedEffect(state.exportMessage) {
        state.exportMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    text = "Track History",
                    style = MaterialTheme.typography.headlineLarge,
                    color = HHColors.TextPrimary
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
            } else if (state.sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Terrain,
                            contentDescription = null,
                            tint = HHColors.TextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No completed tracks yet",
                            fontSize = 14.sp,
                            color = HHColors.TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(state.sessions, key = { it.id }) { session ->
                        TrackSessionRow(
                            session = session,
                            onClick = { onTrackClick(session.id) },
                            onExportClick = { viewModel.exportSession(session.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun TrackSessionRow(
    session: TrackSession,
    onClick: () -> Unit,
    onExportClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    val startDate = dateFormat.format(Date(session.startTime))
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
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Session info
            Column(modifier = Modifier.weight(1f)) {
                // Title row: activity type + location
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = session.activityType.displayName(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HHColors.TextPrimary
                    )
                    session.location?.let {
                        if (it.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = it,
                                fontSize = 13.sp,
                                color = HHColors.TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Stats row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = startDate,
                        fontSize = 12.sp,
                        color = HHColors.TextTertiary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = distanceStr,
                        fontSize = 12.sp,
                        color = HHColors.AccentAltitude,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = durationStr,
                        fontSize = 12.sp,
                        color = HHColors.TextTertiary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Export button
            IconButton(
                onClick = onExportClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(HHColors.SurfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Export KML",
                    tint = HHColors.AccentAltitude,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        HorizontalDivider(
            color = HHColors.BorderSubtle,
            thickness = 1.dp,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return if (h > 0) "${h}h ${String.format("%02d", m)}m"
    else "${m}m"
}
