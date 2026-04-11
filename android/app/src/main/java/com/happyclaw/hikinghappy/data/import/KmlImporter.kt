package com.happyclaw.hikinghappy.data.import

import android.util.Xml
import com.happyclaw.hikinghappy.data.local.entity.ActivityType
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * KML parser supporting two formats:
 * 1. HikingHappy export: <Placemark><LineString><coordinates> with TimeSpan
 * 2. Third-party (e.g. 2bulu): <Folder><Placemark><Point> with TimeStamp + ExtendedData
 *
 * Uses Android native XmlPullParser — no external dependencies.
 */
object KmlImporter {

    private val ISO_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    private val ISO_DATE_FORMAT_MS = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    private val LOCAL_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun parse(inputStream: InputStream): ParsedTrack {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, "UTF-8")
        parser.nextTag()
        return readKml(parser)
    }

    private fun readKml(parser: XmlPullParser): ParsedTrack {
        var name = "Imported Track"
        var location: String? = null
        var startLocationName: String? = null
        var endLocationName: String? = null
        val lineCoords = mutableListOf<Triple<Double, Double, Double>>()
        val extendedPoints = mutableListOf<ParsedPoint>()
        var timeSpanBegin: Long? = null
        var timeSpanEnd: Long? = null

        parser.require(XmlPullParser.START_TAG, null, "kml")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "Document" -> {
                    val docInfo = readDocument(parser, lineCoords, extendedPoints)
                    if (docInfo.name.isNotBlank()) name = docInfo.name
                    if (docInfo.location != null) location = docInfo.location
                    if (startLocationName == null) startLocationName = docInfo.startLocationName
                    if (endLocationName == null) endLocationName = docInfo.endLocationName
                    if (timeSpanBegin == null) timeSpanBegin = docInfo.timeBegin
                    if (timeSpanEnd == null) timeSpanEnd = docInfo.timeEnd
                }
                else -> skip(parser)
            }
        }

        // Build point list: prefer LineString coords (actual GPS track) over individual Point elements
        // (which may be just annotations/waypoints in 2bulu format)
        val points = if (lineCoords.isNotEmpty()) {
            derivePointsFromCoords(lineCoords, timeSpanBegin, timeSpanEnd)
        } else if (extendedPoints.isNotEmpty()) {
            extendedPoints.sortedBy { it.timestamp }
        } else {
            throw IllegalArgumentException("KML contains no track data")
        }

        val startTime = points.firstOrNull()?.timestamp ?: timeSpanBegin ?: System.currentTimeMillis()
        val endTime = points.lastOrNull()?.timestamp ?: timeSpanEnd ?: startTime

        return ParsedTrack(
            name = name,
            activityType = detectActivityType(name),
            location = location,
            startTime = startTime,
            endTime = endTime,
            points = points,
            startLocationName = startLocationName,
            endLocationName = endLocationName
        )
    }

    private data class DocInfo(
        val name: String,
        val location: String?,
        val startLocationName: String?,
        val endLocationName: String?,
        val timeBegin: Long?,
        val timeEnd: Long?
    )

    private fun readDocument(
        parser: XmlPullParser,
        lineCoords: MutableList<Triple<Double, Double, Double>>,
        extendedPoints: MutableList<ParsedPoint>
    ): DocInfo {
        var name = ""
        var location: String? = null
        var startLocationName: String? = null
        var endLocationName: String? = null
        var timeBegin: Long? = null
        var timeEnd: Long? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "name" -> name = readText(parser)
                "description" -> readText(parser) // Skip description
                "TimeSpan" -> {
                    val span = readTimeSpan(parser)
                    timeBegin = span.first
                    timeEnd = span.second
                }
                "Folder", "Placemark" -> {
                    readPlacemarkOrFolder(parser, lineCoords, extendedPoints)
                }
                "ExtendedData" -> {
                    val data = readExtendedDataMap(parser)
                    data["PosStartName"]?.let { if (it.isNotBlank()) startLocationName = it }
                    data["PosEndName"]?.let { if (it.isNotBlank()) endLocationName = it }
                    // Extract time from 2bulu millisecond timestamps (override TimeSpan if available)
                    data["BeginTime"]?.toLongOrNull()?.let { timeBegin = it }
                    data["EndTime"]?.toLongOrNull()?.let { timeEnd = it }
                }
                else -> skip(parser)
            }
        }
        return DocInfo(name, location, startLocationName, endLocationName, timeBegin, timeEnd)
    }

    private fun readPlacemarkOrFolder(
        parser: XmlPullParser,
        lineCoords: MutableList<Triple<Double, Double, Double>>,
        extendedPoints: MutableList<ParsedPoint>
    ) {
        // Could be <Folder> containing Placemarks, or a direct <Placemark>
        if (parser.name == "Folder") {
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.eventType != XmlPullParser.START_TAG) continue
                if (parser.name == "Placemark") {
                    readSinglePlacemark(parser, lineCoords, extendedPoints)
                } else {
                    skip(parser)
                }
            }
        } else {
            readSinglePlacemark(parser, lineCoords, extendedPoints)
        }
    }

    private fun readSinglePlacemark(
        parser: XmlPullParser,
        lineCoords: MutableList<Triple<Double, Double, Double>>,
        extendedPoints: MutableList<ParsedPoint>
    ) {
        var timestamp: Long? = null
        var speed: Float? = null
        var accuracy: Float? = null
        var extTime: Long? = null
        var lat: Double? = null
        var lon: Double? = null
        var alt: Double? = null
        var hasLineString = false

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "TimeStamp" -> timestamp = readTimeStamp(parser)
                "TimeSpan" -> {
                    val span = readTimeSpan(parser)
                    if (timestamp == null) timestamp = span.first
                }
                "Point" -> {
                    val coord = readPoint(parser)
                    lat = coord.first; lon = coord.second; alt = coord.third
                }
                "LineString" -> {
                    hasLineString = true
                    readLineString(parser, lineCoords)
                }
                "ExtendedData" -> {
                    val data = readExtendedDataMap(parser)
                    data["Speed"]?.toFloatOrNull()?.let { speed = it }
                    data["Accuracy"]?.toFloatOrNull()?.let { accuracy = it }
                    data["Time"]?.toLongOrNull()?.let { extTime = it }
                }
                else -> skip(parser)
            }
        }

        // If this Placemark has a Point with coordinates, create a ParsedPoint
        if (lat != null && lon != null) {
            extendedPoints.add(
                ParsedPoint(
                    latitude = lat,
                    longitude = lon,
                    altitude = alt ?: 0.0,
                    speed = speed ?: 0f,
                    accuracy = accuracy,
                    timestamp = extTime ?: timestamp ?: System.currentTimeMillis()
                )
            )
        }
        // LineString coords are already added to lineCoords
    }

    private fun readLineString(
        parser: XmlPullParser,
        lineCoords: MutableList<Triple<Double, Double, Double>>
    ) {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "coordinates") {
                val text = readText(parser).trim()
                parseCoordString(text, lineCoords)
            } else {
                skip(parser)
            }
        }
    }

    private fun readPoint(parser: XmlPullParser): Triple<Double, Double, Double> {
        var lat = 0.0; var lon = 0.0; var alt = 0.0
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "coordinates") {
                val text = readText(parser).trim()
                val parts = text.split(",")
                if (parts.size >= 2) {
                    lon = parts[0].trim().toDoubleOrNull() ?: 0.0
                    lat = parts[1].trim().toDoubleOrNull() ?: 0.0
                    if (parts.size >= 3) alt = parts[2].trim().toDoubleOrNull() ?: 0.0
                }
            } else {
                skip(parser)
            }
        }
        return Triple(lat, lon, alt)
    }

    private fun readTimeStamp(parser: XmlPullParser): Long? {
        var whenText: String? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "when") {
                whenText = readText(parser)
            }
        }
        return whenText?.let { parseTime(it) }
    }

    private fun readTimeSpan(parser: XmlPullParser): Pair<Long?, Long?> {
        var begin: Long? = null; var end: Long? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "begin" -> begin = readText(parser).let { parseTime(it) ?: parseLocalTime(it) }
                "end" -> end = readText(parser).let { parseTime(it) ?: parseLocalTime(it) }
                else -> skip(parser)
            }
        }
        return Pair(begin, end)
    }

    private fun readExtendedDataMap(parser: XmlPullParser): Map<String, String> {
        val result = mutableMapOf<String, String>()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "Data") {
                val key = parser.getAttributeValue(null, "name")
                var value: String? = null
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.eventType == XmlPullParser.START_TAG && parser.name == "value") {
                        value = readText(parser)
                    }
                }
                if (key != null && value != null) {
                    result[key] = value
                }
            } else {
                skip(parser)
            }
        }
        return result
    }

    // --- Helpers ---

    private fun parseCoordString(
        text: String,
        out: MutableList<Triple<Double, Double, Double>>
    ) {
        // Format: "lon1,lat1,alt1 lon2,lat2,alt2 ..."
        for (tuple in text.split("\\s+".toRegex())) {
            val parts = tuple.trim().split(",")
            if (parts.size >= 2) {
                val lon = parts[0].trim().toDoubleOrNull() ?: continue
                val lat = parts[1].trim().toDoubleOrNull() ?: continue
                val alt = if (parts.size >= 3) parts[2].trim().toDoubleOrNull() ?: 0.0 else 0.0
                out.add(Triple(lat, lon, alt))
            }
        }
    }

    /**
     * Derive evenly-spaced points from LineString coordinates when no per-point timestamps exist.
     */
    private fun derivePointsFromCoords(
        coords: List<Triple<Double, Double, Double>>,
        timeBegin: Long?,
        timeEnd: Long?
    ): List<ParsedPoint> {
        if (coords.isEmpty()) return emptyList()

        val start = timeBegin ?: System.currentTimeMillis()
        val end = timeEnd ?: (start + coords.size * 1000L)
        val interval = if (coords.size > 1) (end - start) / (coords.size - 1) else 0L

        return coords.mapIndexed { i, (lat, lon, alt) ->
            ParsedPoint(
                latitude = lat,
                longitude = lon,
                altitude = alt,
                speed = 0f,
                accuracy = null,
                timestamp = start + i * interval
            )
        }
    }

    private fun parseTime(text: String): Long? {
        return parseIsoTime(text) ?: parseIsoTimeMs(text)
    }

    private fun parseIsoTime(text: String): Long? {
        return try { ISO_DATE_FORMAT.parse(text)?.time } catch (_: Exception) { null }
    }

    private fun parseIsoTimeMs(text: String): Long? {
        return try { ISO_DATE_FORMAT_MS.parse(text)?.time } catch (_: Exception) { null }
    }

    private fun parseLocalTime(text: String): Long? {
        return try { LOCAL_DATE_FORMAT.parse(text)?.time } catch (_: Exception) { null }
    }

    private fun detectActivityType(name: String): ActivityType {
        return when {
            name.contains("徒步") || name.contains("登山") || name.contains("Hiking", ignoreCase = true)
            -> ActivityType.HIKING
            name.contains("步行") || name.contains("Walking", ignoreCase = true)
            -> ActivityType.WALKING
            name.contains("骑行") || name.contains("Cycling", ignoreCase = true)
            -> ActivityType.CYCLING
            name.contains("跑步") || name.contains("Running", ignoreCase = true)
            -> ActivityType.RUNNING
            else -> ActivityType.HIKING
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
        }
        parser.nextTag()
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) return
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}
