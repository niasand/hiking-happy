package com.happyclaw.hikinghappy.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyclaw.hikinghappy.service.SyncPhase
import com.happyclaw.hikinghappy.ui.theme.HHColors

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val activityType by viewModel.activityType.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val altitudeUnitIndex by viewModel.altitudeUnitIndex.collectAsStateWithLifecycle()
    val speedUnitIndex by viewModel.speedUnitIndex.collectAsStateWithLifecycle()
    val syncProgress by viewModel.syncProgress.collectAsStateWithLifecycle()

    val altitudeUnitLabel = if (altitudeUnitIndex == 0) "Meters" else "Feet"
    val speedUnitLabel = if (speedUnitIndex == 0) "km/h" else "mph"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HHColors.Background)
    ) {
        // Top App Bar with back
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
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                color = HHColors.TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // DATA section
            SectionHeader(title = "DATA")

            SettingsRow(
                icon = Icons.Default.CloudUpload,
                title = "Sync & Backup",
                onClick = { viewModel.startBackup() }
            )

            SettingsRow(
                icon = Icons.Default.CloudDownload,
                title = "Restore Data",
                onClick = { viewModel.startRestore() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // UNITS section
            SectionHeader(title = "UNITS")

            SettingsRow(
                icon = null,
                title = "Altitude",
                value = altitudeUnitLabel,
                onClick = { viewModel.setAltitudeUnitIndex(if (altitudeUnitIndex == 0) 1 else 0) }
            )

            SettingsRow(
                icon = null,
                title = "Speed",
                value = speedUnitLabel,
                onClick = { viewModel.setSpeedUnitIndex(if (speedUnitIndex == 0) 1 else 0) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ABOUT section
            SectionHeader(title = "ABOUT")

            SettingsRow(
                icon = null,
                title = "Version",
                value = "1.0.0",
                onClick = {}
            )
        }
    }

    // Sync progress dialog
    syncProgress?.let { progress ->
        SyncProgressDialog(
            progress = progress,
            onDismiss = { viewModel.dismissSyncProgress() },
            onRetry = {
                viewModel.dismissSyncProgress()
                if (progress.message.contains("Upload", ignoreCase = true) ||
                    progress.message.contains("Backup", ignoreCase = true)) {
                    viewModel.startBackup()
                } else {
                    viewModel.startRestore()
                }
            }
        )
    }
}

@Composable
private fun SyncProgressDialog(
    progress: com.happyclaw.hikinghappy.service.SyncProgress,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    val isComplete = progress.phase == SyncPhase.COMPLETE
    val isError = progress.phase == SyncPhase.ERROR
    val isInProgress = progress.phase == SyncPhase.IN_PROGRESS

    AlertDialog(
        onDismissRequest = { if (isComplete || isError) onDismiss() },
        containerColor = HHColors.Surface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = when (progress.phase) {
                    SyncPhase.PREPARING, SyncPhase.IN_PROGRESS -> "Sync & Backup"
                    SyncPhase.COMPLETE -> "Success"
                    SyncPhase.ERROR -> "Sync Failed"
                },
                style = MaterialTheme.typography.headlineLarge,
                color = HHColors.TextPrimary
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isError) {
                    Text(
                        text = progress.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = HHColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = onRetry,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = HHColors.Error
                        )
                    ) {
                        Text("Retry", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    if (isInProgress) {
                        LinearProgressIndicator(
                            progress = { progress.percentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = if (isError) HHColors.Error else HHColors.AccentAltitude,
                            trackColor = HHColors.SurfaceElevated,
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Text(
                        text = progress.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = HHColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    if (isComplete) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onDismiss) {
                            Text("OK", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.bodySmall.copy(
            letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing
        ),
        color = HHColors.TextTertiary
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector?,
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HHColors.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = HHColors.TextSecondary,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = HHColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = HHColors.TextSecondary
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = HHColors.TextTertiary,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
    HorizontalDivider(
        color = HHColors.BorderSubtle,
        thickness = 1.dp,
        modifier = Modifier.padding(start = if (icon != null) 56.dp else 16.dp)
    )
}
