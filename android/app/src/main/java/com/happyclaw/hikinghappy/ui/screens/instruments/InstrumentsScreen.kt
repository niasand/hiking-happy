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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyclaw.hikinghappy.data.local.entity.ActivityType
import com.happyclaw.hikinghappy.domain.model.GpsSignalState
import com.happyclaw.hikinghappy.ui.components.AcquiringGpsState
import com.happyclaw.hikinghappy.ui.components.AmapView
import com.happyclaw.hikinghappy.ui.components.GpsPermissionHandler
import com.happyclaw.hikinghappy.ui.theme.HHColors

@Composable
fun InstrumentsScreen(
    viewModel: InstrumentsViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit
) {
    GpsPermissionHandler {
        val state by viewModel.state.collectAsStateWithLifecycle()

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HHColors.Background)
                .keepScreenOn()
        ) {
            TopAppBar(onSettingsClick = onSettingsClick)

            // Top section: altitude & speed cards (scrollable)
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Altitude & Speed side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InstrumentCard(
                        modifier = Modifier.weight(1f),
                        label = "ALTITUDE",
                        accentColor = HHColors.AccentAltitude,
                        value = if (state.altitude.isNaN()) "---"
                                else String.format("%,.0f", state.getAltitudeUnit().conversion(state.altitude)),
                        unit = state.altitudeUnitLabel,
                        gpsState = state.gpsState,
                        isGpsAcquiring = state.isGpsAcquiring
                    )
                    InstrumentCard(
                        modifier = Modifier.weight(1f),
                        label = "SPEED",
                        accentColor = HHColors.AccentSpeed,
                        value = String.format("%.1f", state.getSpeedUnit().conversion(state.displaySpeed)),
                        unit = state.speedUnitLabel,
                        gpsState = null
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Map — fixed, NOT inside scrollable container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, HHColors.BorderSubtle, RoundedCornerShape(12.dp))
            ) {
                AmapView(
                    latitude = state.latitude,
                    longitude = state.longitude,
                    hasFix = !state.altitude.isNaN() && state.gpsState != GpsSignalState.LOST,
                    refreshTrigger = state.locationRefreshCounter,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )

                // Locate button — bottom-right
                IconButton(
                    onClick = { viewModel.refreshLocation() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 8.dp, bottom = 8.dp)
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "Locate",
                        tint = androidx.compose.ui.graphics.Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Bottom section: location input & activity type
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                LocationInput(
                    location = state.location,
                    onLocationChange = { viewModel.setLocation(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ActivityTypeSelector(
                    selectedType = state.activityType,
                    onTypeSelected = { viewModel.setActivityType(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// --- Top App Bar ---

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

// --- Instrument Card (altitude or speed) ---

@Composable
private fun InstrumentCard(
    modifier: Modifier = Modifier,
    label: String,
    accentColor: Color,
    value: String,
    unit: String,
    gpsState: GpsSignalState?,
    isGpsAcquiring: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(HHColors.Surface)
            .border(1.dp, HHColors.BorderSubtle, RoundedCornerShape(12.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label row with GPS dot (altitude only)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (gpsState != null) {
                val dotColor = when (gpsState) {
                    GpsSignalState.ACTIVE -> HHColors.GpsActive
                    GpsSignalState.WEAK -> HHColors.GpsWeak
                    GpsSignalState.POOR -> HHColors.GpsPoor
                    GpsSignalState.LOST -> HHColors.GpsLost
                }
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = HHColors.TextTertiary,
                letterSpacing = androidx.compose.ui.unit.TextUnit(0.3f, androidx.compose.ui.unit.TextUnitType.Sp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Accent divider
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(1.dp)
                .background(accentColor)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Value
        val alpha = if (gpsState == GpsSignalState.LOST && !isGpsAcquiring) 0.4f else 1f
        Text(
            text = if (value == "---") "---" else "$value $unit",
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.SansSerif,
            color = HHColors.TextPrimary.copy(alpha = alpha),
            textAlign = TextAlign.Center
        )

        // GPS warning (altitude only)
        if (gpsState == GpsSignalState.LOST && !isGpsAcquiring) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "No Signal",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = HHColors.Warning
            )
        }
    }
}

// --- Location Input ---

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
                text = "活动地点",
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

// --- Activity Type Selector ---

@Composable
private fun ActivityTypeSelector(
    selectedType: ActivityType,
    onTypeSelected: (ActivityType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
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
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                fontSize = 13.sp,
                fontWeight = fontWeight,
                color = textColor
            )
        }
    }
}
