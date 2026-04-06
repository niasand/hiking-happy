package com.happyclaw.hikinghappy.ui.components

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.happyclaw.hikinghappy.ui.theme.HHColors
import kotlinx.coroutines.delay

@Composable
fun GpsPermissionHandler(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by rememberSaveable { mutableStateOf(false) }
    var showRationale by rememberSaveable { mutableStateOf(false) }
    var permissionChecked by rememberSaveable { mutableStateOf(false) }

    // Check existing permission state on first composition
    LaunchedEffect(Unit) {
        hasPermission = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        permissionChecked = true
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) {
            showRationale = true
        }
        permissionChecked = true
    }

    if (!permissionChecked || !hasPermission) {
        if (showRationale) {
            PermissionDeniedScreen(
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            )
        } else {
            // Show acquiring state while permission is being requested
            AcquiringGpsState()
        }
    } else {
        content()
    }
}

@Composable
fun PermissionDeniedScreen(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HHColors.Background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOff,
            contentDescription = "Location disabled",
            tint = HHColors.Warning,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Location Permission Required",
            style = MaterialTheme.typography.headlineLarge,
            color = HHColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enable location access to use\naltitude and speed tracking.",
            style = MaterialTheme.typography.bodyMedium,
            color = HHColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onOpenSettings,
            colors = ButtonDefaults.buttonColors(
                containerColor = HHColors.AccentAltitude,
                contentColor = HHColors.TextInverse
            )
        ) {
            Text(
                text = "Enable Location",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun AcquiringGpsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HHColors.Background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var pulseAlpha by rememberSaveable { mutableStateOf(1f) }

        LaunchedEffect(Unit) {
            while (true) {
                // Pulse cycle: 1.5s total
                delay(750)
                pulseAlpha = 0.3f
                delay(750)
                pulseAlpha = 1f
            }
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(HHColors.AccentAltitude.copy(alpha = pulseAlpha))
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Acquiring GPS signal...",
            style = MaterialTheme.typography.bodyLarge,
            color = HHColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
