package com.happyclaw.hikinghappy.ui.screens.trends

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyclaw.hikinghappy.data.local.entity.ActivityRecord
import com.happyclaw.hikinghappy.ui.theme.HHColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrendsScreen(
    viewModel: TrendsViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit
) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    val altitudeUnitIndex by viewModel.altitudeUnitIndex.collectAsStateWithLifecycle()
    val speedUnitIndex by viewModel.speedUnitIndex.collectAsStateWithLifecycle()

    val altitudeUnitLabel = if (altitudeUnitIndex == 0) "m" else "ft"
    val altitudeConversion = if (altitudeUnitIndex == 0) { m: Double -> m } else { m: Double -> m * 3.28084 }
    val speedUnitLabel = if (speedUnitIndex == 0) "km/h" else "mph"
    val speedConversion = if (speedUnitIndex == 0) { s: Double -> s * 3.6 } else { s: Double -> s * 2.23694 }

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
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HikingHappy",
                style = MaterialTheme.typography.headlineLarge,
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

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Altitude chart title
            Text(
                text = "Altitude (2h)",
                style = MaterialTheme.typography.headlineMedium,
                color = HHColors.TextPrimary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // Altitude chart container
            ChartContainer(records.isNotEmpty()) {
                if (records.isEmpty()) {
                    EmptyChartState(
                        "No altitude data yet.",
                        "Start an activity to see your trend."
                    )
                } else {
                    AltitudeChart(
                        records = records,
                        unitConversion = altitudeConversion,
                        unitLabel = altitudeUnitLabel
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Speed chart title
            Text(
                text = "Speed (2h)",
                style = MaterialTheme.typography.headlineMedium,
                color = HHColors.TextPrimary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // Speed chart container
            ChartContainer(records.isNotEmpty()) {
                if (records.isEmpty()) {
                    EmptyChartState(
                        "No speed data yet.",
                        "Start an activity to see your trend."
                    )
                } else {
                    SpeedChart(
                        records = records,
                        unitConversion = speedConversion,
                        unitLabel = speedUnitLabel
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ChartContainer(hasData: Boolean, content: @Composable () -> Unit) {
    val config = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = config.screenWidthDp < 412
    val chartHeight = if (isSmallScreen) 160.dp else 200.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(chartHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(HHColors.Surface)
            .border(1.dp, HHColors.BorderSubtle, RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun EmptyChartState(message: String, hint: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Terrain,
            contentDescription = null,
            tint = HHColors.TextTertiary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = HHColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = hint,
            fontSize = 12.sp,
            color = HHColors.TextTertiary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Simplified altitude chart using Canvas.
 * Downsample: show every 6th point (7200 -> ~1200 visible points).
 */
@Composable
private fun AltitudeChart(
    records: List<ActivityRecord>,
    unitConversion: (Double) -> Double,
    unitLabel: String
) {
    // Downsample for performance
    val sampled = records.filterIndexed { index, _ -> index % 6 == 0 }
    if (sampled.isEmpty()) return

    val altitudes = sampled.map { unitConversion(it.altitude) }
    val minAlt = altitudes.minOrNull() ?: 0.0
    val maxAlt = altitudes.maxOrNull() ?: 0.0
    val range = maxAlt - minAlt
    val padding = range * 0.1
    val yMin = minAlt - padding
    val yMax = maxAlt + padding

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val startTime = sampled.first().timestamp
    val endTime = sampled.last().timestamp
    val timeRange = (endTime - startTime).toFloat()

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp, bottom = 20.dp)
    ) {
        if (timeRange <= 0f || sampled.size < 2) return@Canvas

        val chartLeft = 0f
        val chartRight = size.width
        val chartTop = 0f
        val chartBottom = size.height
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        // Draw horizontal grid lines (2 lines: min, max)
        val gridColor = HHColors.BorderSubtle
        drawLine(
            color = gridColor,
            start = androidx.compose.ui.geometry.Offset(chartLeft, chartTop),
            end = androidx.compose.ui.geometry.Offset(chartRight, chartTop),
            strokeWidth = 1f
        )
        drawLine(
            color = gridColor,
            start = androidx.compose.ui.geometry.Offset(chartLeft, chartBottom),
            end = androidx.compose.ui.geometry.Offset(chartRight, chartBottom),
            strokeWidth = 1f
        )

        // Build line path
        val points = sampled.mapIndexed { index, record ->
            val x = chartLeft + ((record.timestamp - startTime).toFloat() / timeRange) * chartWidth
            val y = (chartBottom - ((unitConversion(record.altitude) - yMin) / (yMax - yMin)) * chartHeight).toFloat()
            androidx.compose.ui.geometry.Offset(x, y.coerceIn(chartTop, chartBottom))
        }

        // Draw area fill (gradient from 12% opacity to 0%)
        if (points.size >= 2) {
            val areaPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
                lineTo(points.last().x, chartBottom)
                lineTo(points.first().x, chartBottom)
                close()
            }
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        HHColors.AccentAltitude.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    startY = chartTop,
                    endY = chartBottom
                )
            )

            // Draw line
            val linePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = linePath,
                color = HHColors.AccentAltitude,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.dp.toPx()
                )
            )
        }

        // Y-axis labels (min, max)
        val labelPaint = android.graphics.Paint().apply {
            color = HHColors.TextTertiary.hashCode()
            textSize = 11.sp.toPx()
            typeface = android.graphics.Typeface.MONOSPACE
            textAlign = android.graphics.Paint.Align.RIGHT
        }
        drawIntoCanvas {
            it.nativeCanvas.drawText(
                String.format("%,.0f", yMin),
                chartRight - 8.dp.toPx(),
                chartBottom - 2.dp.toPx(),
                labelPaint
            )
            it.nativeCanvas.drawText(
                String.format("%,.0f", yMax),
                chartRight - 8.dp.toPx(),
                chartTop + 14.sp.toPx(),
                labelPaint
            )
        }

        // X-axis time labels (30-min intervals)
        val labelPaintBottom = android.graphics.Paint().apply {
            color = HHColors.TextTertiary.hashCode()
            textSize = 11.sp.toPx()
            typeface = android.graphics.Typeface.MONOSPACE
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val intervalMs = 30 * 60 * 1000L
        var labelTime = startTime - (startTime % intervalMs) + intervalMs
        while (labelTime <= endTime) {
            val x = chartLeft + ((labelTime - startTime).toFloat() / timeRange) * chartWidth
            if (x in chartLeft..chartRight) {
                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        timeFormat.format(Date(labelTime)),
                        x,
                        chartBottom + 16.dp.toPx(),
                        labelPaintBottom
                    )
                }
            }
            labelTime += intervalMs
        }
    }
}

/**
 * Simplified speed chart using Canvas.
 */
@Composable
private fun SpeedChart(
    records: List<ActivityRecord>,
    unitConversion: (Double) -> Double,
    unitLabel: String
) {
    // Downsample for performance
    val sampled = records.filterIndexed { index, _ -> index % 6 == 0 }
    if (sampled.isEmpty()) return

    val speeds = sampled.map { unitConversion(it.speed) }
    val minSpd = speeds.minOrNull() ?: 0.0
    val maxSpd = speeds.maxOrNull() ?: 0.0
    val range = maxSpd - minSpd
    val padding = if (range < 1.0) 0.5 else range * 0.1
    val yMin = (minSpd - padding).coerceAtLeast(0.0)
    val yMax = maxSpd + padding

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val startTime = sampled.first().timestamp
    val endTime = sampled.last().timestamp
    val timeRange = (endTime - startTime).toFloat()

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp, bottom = 20.dp)
    ) {
        if (timeRange <= 0f || sampled.size < 2) return@Canvas

        val chartLeft = 0f
        val chartRight = size.width
        val chartTop = 0f
        val chartBottom = size.height
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        // Draw horizontal grid lines
        drawLine(
            color = HHColors.BorderSubtle,
            start = androidx.compose.ui.geometry.Offset(chartLeft, chartTop),
            end = androidx.compose.ui.geometry.Offset(chartRight, chartTop),
            strokeWidth = 1f
        )
        drawLine(
            color = HHColors.BorderSubtle,
            start = androidx.compose.ui.geometry.Offset(chartLeft, chartBottom),
            end = androidx.compose.ui.geometry.Offset(chartRight, chartBottom),
            strokeWidth = 1f
        )

        // Build line path
        val points = sampled.mapIndexed { index, record ->
            val x = chartLeft + ((record.timestamp - startTime).toFloat() / timeRange) * chartWidth
            val y = (chartBottom - ((unitConversion(record.speed) - yMin) / (yMax - yMin)) * chartHeight).toFloat()
            androidx.compose.ui.geometry.Offset(x, y.coerceIn(chartTop, chartBottom))
        }

        // Draw area fill
        if (points.size >= 2) {
            val areaPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
                lineTo(points.last().x, chartBottom)
                lineTo(points.first().x, chartBottom)
                close()
            }
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        HHColors.AccentSpeed.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    startY = chartTop,
                    endY = chartBottom
                )
            )

            // Draw line
            val linePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = linePath,
                color = HHColors.AccentSpeed,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.dp.toPx()
                )
            )
        }

        // Y-axis labels
        val labelPaint = android.graphics.Paint().apply {
            color = HHColors.TextTertiary.hashCode()
            textSize = 11.sp.toPx()
            typeface = android.graphics.Typeface.MONOSPACE
            textAlign = android.graphics.Paint.Align.RIGHT
        }
        drawIntoCanvas {
            it.nativeCanvas.drawText(
                String.format("%.1f", yMin),
                chartRight - 8.dp.toPx(),
                chartBottom - 2.dp.toPx(),
                labelPaint
            )
            it.nativeCanvas.drawText(
                String.format("%.1f", yMax),
                chartRight - 8.dp.toPx(),
                chartTop + 14.sp.toPx(),
                labelPaint
            )
        }

        // X-axis time labels
        val labelPaintBottom = android.graphics.Paint().apply {
            color = HHColors.TextTertiary.hashCode()
            textSize = 11.sp.toPx()
            typeface = android.graphics.Typeface.MONOSPACE
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val intervalMs = 30 * 60 * 1000L
        var labelTime = startTime - (startTime % intervalMs) + intervalMs
        while (labelTime <= endTime) {
            val x = chartLeft + ((labelTime - startTime).toFloat() / timeRange) * chartWidth
            if (x in chartLeft..chartRight) {
                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        timeFormat.format(Date(labelTime)),
                        x,
                        chartBottom + 16.dp.toPx(),
                        labelPaintBottom
                    )
                }
            }
            labelTime += intervalMs
        }
    }
}
