package com.happyclaw.hikinghappy.ui.screens.trends

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
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

// --- Data structures ---

private data class ChartPoint(
    val x: Float,
    val y: Float,
    val rawValue: Double,
    val timestamp: Long
)

// --- Main Screen ---

@Composable
fun TrendsScreen(
    viewModel: TrendsViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit
) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    val selectedRange by viewModel.selectedRange.collectAsStateWithLifecycle()
    val altitudeUnitIndex by viewModel.altitudeUnitIndex.collectAsStateWithLifecycle()
    val speedUnitIndex by viewModel.speedUnitIndex.collectAsStateWithLifecycle()

    val altitudeConversion = if (altitudeUnitIndex == 0) { m: Double -> m } else { m: Double -> m * 3.28084 }
    val altitudeUnitLabel = if (altitudeUnitIndex == 0) "m" else "ft"
    val speedConversion = if (speedUnitIndex == 0) { s: Double -> s * 3.6 } else { s: Double -> s * 2.23694 }
    val speedUnitLabel = if (speedUnitIndex == 0) "km/h" else "mph"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HHColors.Background)
    ) {
        TopAppBar(onSettingsClick = onSettingsClick)

        TimeRangeSelector(
            selected = selectedRange,
            onRangeSelected = viewModel::setTimeRange
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Altitude section
            val altitudes = records.map { altitudeConversion(it.altitude) }
            ChartSectionHeader(
                title = "Altitude",
                unitLabel = altitudeUnitLabel,
                values = altitudes,
                accentColor = HHColors.AccentAltitude
            )

            ChartContainer(records.isNotEmpty()) {
                if (records.isEmpty()) {
                    EmptyChartState("No altitude data yet.", "Start an activity to see your trend.")
                } else {
                    LineChart(
                        records = records,
                        valueExtractor = { altitudeConversion(it.altitude) },
                        color = HHColors.AccentAltitude,
                        labelFormat = { v -> String.format("%,.0f", v) },
                        unitLabel = altitudeUnitLabel
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Speed section
            val speeds = records.map { speedConversion(it.speed) }
            ChartSectionHeader(
                title = "Speed",
                unitLabel = speedUnitLabel,
                values = speeds,
                accentColor = HHColors.AccentSpeed
            )

            ChartContainer(records.isNotEmpty()) {
                if (records.isEmpty()) {
                    EmptyChartState("No speed data yet.", "Start an activity to see your trend.")
                } else {
                    LineChart(
                        records = records,
                        valueExtractor = { speedConversion(it.speed) },
                        color = HHColors.AccentSpeed,
                        labelFormat = { v -> String.format("%.1f", v) },
                        unitLabel = speedUnitLabel
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
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
}

// --- Time Range Selector ---

@Composable
private fun TimeRangeSelector(
    selected: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HHColors.Surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeRange.entries.forEach { range ->
            val isSelected = range == selected
            val bgColor = if (isSelected) HHColors.SurfaceElevated else Color.Transparent
            val textColor = if (isSelected) HHColors.TextPrimary else HHColors.TextTertiary
            val borderColor = if (isSelected) HHColors.BorderStandard else Color.Transparent

            Text(
                text = range.label,
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(9999.dp))
                    .clickable { onRangeSelected(range) }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

// --- Chart Section Header with stats ---

@Composable
private fun ChartSectionHeader(
    title: String,
    unitLabel: String,
    values: List<Double>,
    accentColor: Color
) {
    val min = values.minOrNull()
    val max = values.maxOrNull()
    val avg = if (values.isNotEmpty()) values.sum() / values.size else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "$title (${unitLabel})",
            style = MaterialTheme.typography.headlineMedium,
            color = HHColors.TextPrimary
        )
        Spacer(modifier = Modifier.weight(1f))
        if (min != null && max != null && avg != null) {
            Text(
                text = "H:${String.format("%,.0f", max)}  A:${String.format("%,.0f", avg)}  L:${String.format("%,.0f", min)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = accentColor,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

// --- Chart Container ---

@Composable
private fun ChartContainer(hasData: Boolean, content: @Composable () -> Unit) {
    val config = LocalConfiguration.current
    val isSmallScreen = config.screenWidthDp < 412
    val chartHeight = if (isSmallScreen) 180.dp else 220.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(chartHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(HHColors.Surface)
            .border(1.dp, HHColors.BorderSubtle, RoundedCornerShape(12.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// --- Empty State ---

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

// --- Generic Line Chart ---

@Composable
private fun LineChart(
    records: List<ActivityRecord>,
    valueExtractor: (ActivityRecord) -> Double,
    color: Color,
    labelFormat: (Double) -> String,
    unitLabel: String
) {
    // Downsample for performance (every 6th point)
    val raw = records.filterIndexed { index, _ -> index % 6 == 0 }
    if (raw.size < 2) return

    // Apply 5-point moving average smoothing
    val smoothed = smooth(raw, valueExtractor)

    // Compute value range
    val values = smoothed.map { it.rawValue }
    val minVal = values.minOrNull() ?: return
    val maxVal = values.maxOrNull() ?: return
    val range = maxVal - minVal
    val yPadding = if (range < 1.0) 0.5 else range * 0.1
    val yMin = (minVal - yPadding).coerceAtLeast(0.0)
    val yMax = maxVal + yPadding
    val yRange = yMax - yMin
    if (yRange <= 0.0) return

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val startTime = smoothed.first().timestamp
    val endTime = smoothed.last().timestamp
    val timeRange = (endTime - startTime).toFloat()

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 40.dp, top = 8.dp, end = 4.dp, bottom = 20.dp)
    ) {
        if (timeRange <= 0f) return@Canvas

        val chartLeft = 0f
        val chartRight = size.width
        val chartTop = 0f
        val chartBottom = size.height
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        // Map data points to canvas coordinates
        val points = smoothed.map { pt ->
            val x = chartLeft + ((pt.timestamp - startTime).toFloat() / timeRange) * chartWidth
            val yNorm = ((pt.rawValue - yMin) / yRange).coerceIn(0.0, 1.0)
            val y = chartBottom - (yNorm * chartHeight).toFloat()
            ChartPoint(x, y, pt.rawValue, pt.timestamp)
        }

        // Grid lines (4 horizontal dashed lines)
        val gridCount = 4
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
        val gridPaint = Stroke(
            width = 1f,
            pathEffect = dashEffect
        )
        for (i in 0..gridCount) {
            val yFrac = i.toFloat() / gridCount
            val yPos = chartTop + yFrac * chartHeight
            drawLine(
                color = HHColors.BorderSubtle,
                start = Offset(chartLeft, yPos),
                end = Offset(chartRight, yPos),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )
            // Y-axis label (left side)
            val yValue = yMax - yFrac * yRange
            drawIntoCanvas {
                val paint = android.graphics.Paint().apply {
                    this.color = HHColors.TextTertiary.hashCode()
                    textSize = 10.sp.toPx()
                    typeface = android.graphics.Typeface.MONOSPACE
                    textAlign = android.graphics.Paint.Align.RIGHT
                    isAntiAlias = true
                }
                it.nativeCanvas.drawText(
                    labelFormat(yValue),
                    chartLeft - 6.dp.toPx(),
                    yPos + 4.sp.toPx(),
                    paint
                )
            }
        }

        // Build smooth cubic bezier path
        if (points.size >= 2) {
            // Area fill with gradient
            val areaPath = buildSmoothPath(points, closed = true, bottom = chartBottom)
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    startY = chartTop,
                    endY = chartBottom
                )
            )

            // Line stroke
            val linePath = buildSmoothPath(points, closed = false)
            drawPath(
                path = linePath,
                color = color,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Latest data point indicator
            val lastCenter = Offset(points.last().x, points.last().y)
            drawCircle(
                color = HHColors.Surface,
                radius = 5.dp.toPx(),
                center = lastCenter
            )
            drawCircle(
                color = color,
                radius = 3.5.dp.toPx(),
                center = lastCenter
            )
        }

        // X-axis time labels
        val intervalMs = computeTimeLabelInterval(timeRange.toLong())
        var labelTime = startTime - (startTime % intervalMs) + intervalMs
        val xLabelPaint = android.graphics.Paint().apply {
            this.color = HHColors.TextTertiary.hashCode()
            textSize = 10.sp.toPx()
            typeface = android.graphics.Typeface.MONOSPACE
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        while (labelTime <= endTime) {
            val x = chartLeft + ((labelTime - startTime).toFloat() / timeRange) * chartWidth
            if (x in (chartLeft + 20.dp.toPx())..(chartRight - 20.dp.toPx())) {
                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        timeFormat.format(Date(labelTime)),
                        x,
                        chartBottom + 14.sp.toPx(),
                        xLabelPaint
                    )
                }
            }
            labelTime += intervalMs
        }
    }
}

// --- Utilities ---

private data class SmoothedPoint(
    val rawValue: Double,
    val timestamp: Long
)

private fun smooth(
    records: List<ActivityRecord>,
    valueExtractor: (ActivityRecord) -> Double,
    windowSize: Int = 5
): List<SmoothedPoint> {
    return records.mapIndexed { index, record ->
        if (index == 0 || records.size < windowSize) {
            SmoothedPoint(valueExtractor(record), record.timestamp)
        } else {
            val half = windowSize / 2
            val start = (index - half).coerceAtLeast(0)
            val end = (index + half + 1).coerceAtMost(records.size)
            val avg = records.subList(start, end).map { valueExtractor(it) }.average()
            SmoothedPoint(avg, record.timestamp)
        }
    }
}

private fun DrawScope.buildSmoothPath(
    points: List<ChartPoint>,
    closed: Boolean,
    bottom: Float = 0f
): Path {
    val path = Path()
    if (points.isEmpty()) return path

    path.moveTo(points[0].x, points[0].y)

    if (points.size == 2) {
        path.lineTo(points[1].x, points[1].y)
    } else {
        // Monotone cubic interpolation for smooth curves
        for (i in 0 until points.size - 1) {
            val p0 = points[(i - 1).coerceAtLeast(0)]
            val p1 = points[i]
            val p2 = points[i + 1]
            val p3 = points[(i + 2).coerceAtMost(points.size - 1)]

            val cp1x = p1.x + (p2.x - p0.x) / 6f
            val cp1y = p1.y + (p2.y - p0.y) / 6f
            val cp2x = p2.x - (p3.x - p1.x) / 6f
            val cp2y = p2.y - (p3.y - p1.y) / 6f

            path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
        }
    }

    if (closed && points.size >= 2) {
        path.lineTo(points.last().x, bottom)
        path.lineTo(points.first().x, bottom)
        path.close()
    }

    return path
}

private fun computeTimeLabelInterval(timeRangeMs: Long): Long {
    return when {
        timeRangeMs <= 60 * 60 * 1000L -> 10 * 60 * 1000L        // 10min for 1h
        timeRangeMs <= 2 * 60 * 60 * 1000L -> 30 * 60 * 1000L    // 30min for 2h
        timeRangeMs <= 4 * 60 * 60 * 1000L -> 60 * 60 * 1000L    // 1h for 4h
        else -> 2 * 60 * 60 * 1000L                               // 2h for today
    }
}
