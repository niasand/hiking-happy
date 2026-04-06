package com.happyclaw.hikinghappy.ui.screens.instruments

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyclaw.hikinghappy.data.local.entity.ActivityType
import com.happyclaw.hikinghappy.domain.model.AltitudeUnit
import com.happyclaw.hikinghappy.domain.model.GpsSignalState
import com.happyclaw.hikinghappy.domain.model.SpeedUnit
import com.happyclaw.hikinghappy.ui.components.AcquiringGpsState
import com.happyclaw.hikinghappy.ui.components.GpsPermissionHandler
import com.happyclaw.hikinghappy.ui.theme.HHColors

@Composable
fun InstrumentsScreen(
    viewModel: InstrumentsViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit
) {
    GpsPermissionHandler {
        val state by viewModel.state.collectAsStateWithLifecycle()

        // Show acquiring GPS state while waiting for first fix
        if (state.isGpsAcquiring && state.altitude.isNaN()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(HHColors.Background)
                    .keepScreenOn()
            ) {
                TopAppBar(onSettingsClick = onSettingsClick)
                AcquiringGpsState()
            }
            return@GpsPermissionHandler
        }

        val config = LocalConfiguration.current
        val isSmallScreen = config.screenWidthDp < 412

        val altitudeDisplaySize = if (isSmallScreen) 48.sp else 56.sp
        val speedDisplaySize = if (isSmallScreen) 36.sp else 42.sp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HHColors.Background)
                .keepScreenOn()
        ) {
            TopAppBar(onSettingsClick = onSettingsClick)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                AltitudeDisplay(
                    altitude = state.altitude,
                    unit = state.getAltitudeUnit(),
                    displaySize = altitudeDisplaySize,
                    gpsState = state.gpsState,
                    hasBarometer = state.hasBarometer,
                    isGpsAcquiring = state.isGpsAcquiring
                )

                Spacer(modifier = Modifier.height(20.dp))

                LocationInput(
                    location = state.location,
                    onLocationChange = { viewModel.setLocation(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ActivityTypeSelector(
                    selectedType = state.activityType,
                    onTypeSelected = { viewModel.setActivityType(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                SpeedDisplay(
                    speed = state.displaySpeed,
                    unit = state.getSpeedUnit(),
                    displaySize = speedDisplaySize,
                    gpsState = state.gpsState
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TopAppBar(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HHColors.Surface)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "HikingHappy",
            style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
            color = HHColors.TextPrimary
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = HHColors.TextSecondary
            )
        }
    }
}

@Composable
private fun AltitudeDisplay(
    altitude: Double,
    unit: AltitudeUnit,
    displaySize: TextUnit,
    gpsState: GpsSignalState,
    hasBarometer: Boolean,
    isGpsAcquiring: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val dotColor = when (gpsState) {
                GpsSignalState.ACTIVE -> HHColors.GpsActive
                GpsSignalState.WEAK -> HHColors.GpsWeak
                GpsSignalState.POOR -> HHColors.GpsPoor
                GpsSignalState.LOST -> HHColors.GpsLost
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ALTITUDE",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = HHColors.TextTertiary,
                letterSpacing = TextUnit(0.3f, TextUnitType.Sp)
            )
            if (!hasBarometer && !isGpsAcquiring) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(GPS only)",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = HHColors.TextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(24.dp)
                .height(1.dp)
                .background(HHColors.AccentAltitude)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (altitude.isNaN() || isGpsAcquiring) {
            Text(
                text = "---",
                fontSize = displaySize,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.SansSerif,
                color = HHColors.TextTertiary,
                letterSpacing = TextUnit(-1.5f, TextUnitType.Sp)
            )
        } else {
            val displayValue = unit.conversion(altitude)
            val formattedValue = String.format("%,.0f", displayValue)
            val alpha = if (gpsState == GpsSignalState.LOST) 0.4f else 1f
            Text(
                text = "$formattedValue ${unit.label}",
                fontSize = displaySize,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.SansSerif,
                color = HHColors.TextPrimary.copy(alpha = alpha),
                letterSpacing = TextUnit(-1.5f, TextUnitType.Sp)
            )
        }

        if (gpsState == GpsSignalState.LOST && !isGpsAcquiring) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No GPS Signal",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = HHColors.Warning,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SpeedDisplay(
    speed: Double,
    unit: SpeedUnit,
    displaySize: TextUnit,
    gpsState: GpsSignalState
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(1.dp)
                .background(HHColors.AccentSpeed)
        )

        Spacer(modifier = Modifier.height(8.dp))

        val displayValue = unit.conversion(speed)
        val formattedSpeed = String.format("%.1f", displayValue)
        Text(
            text = "$formattedSpeed ${unit.label}",
            fontSize = displaySize,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.SansSerif,
            color = HHColors.TextPrimary,
            letterSpacing = TextUnit(-1f, TextUnitType.Sp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "SPEED",
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = HHColors.TextTertiary,
            letterSpacing = TextUnit(0.3f, TextUnitType.Sp)
        )
    }
}

@Composable
private fun LocationInput(
    location: String,
    onLocationChange: (String) -> Unit
) {
    OutlinedTextField(
        value = location,
        onValueChange = { if (it.length <= 200) onLocationChange(it) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Enter location...",
                fontSize = 14.sp,
                color = HHColors.TextTertiary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = HHColors.TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (location.isNotEmpty()) {
                IconButton(onClick = { onLocationChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear location",
                        tint = HHColors.TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HHColors.BorderActive,
            unfocusedBorderColor = HHColors.BorderSubtle,
            cursorColor = HHColors.Info,
            focusedTextColor = HHColors.TextPrimary,
            unfocusedTextColor = HHColors.TextPrimary
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
    )
}

@Composable
private fun ActivityTypeSelector(
    selectedType: ActivityType,
    onTypeSelected: (ActivityType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        ActivityType.entries.forEach { type ->
            val isSelected = type == selectedType
            val bgColor = if (isSelected) HHColors.AccentAltitudeSurface else Color.Transparent
            val textColor = if (isSelected) HHColors.AccentAltitude else HHColors.TextTertiary
            val borderColor = if (isSelected) HHColors.AccentAltitude else HHColors.BorderSubtle
            val fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium

            Text(
                text = type.displayName(),
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(9999.dp))
                    .clickable { onTypeSelected(type) }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                fontSize = 14.sp,
                fontWeight = fontWeight,
                color = textColor
            )
        }
    }
}
