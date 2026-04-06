# PRD: HikingHappy -- Real-Time Altimeter & Speedometer

**Status**: Approved
**Author**: Alex (PM)
**Last Updated**: 2026-04-06
**Version**: 2.0
**Stakeholders**: Engineering Lead, Design Lead, QA Engineer

---

## 1. Product Overview

### 1.1 What Is HikingHappy

HikingHappy is a lightweight, Android-native outdoor activity instrument focused on two core real-time metrics: **altitude** and **speed**. It is designed for hikers, walkers, cyclists, runners, and mountaineers who want a clean, instant-at-a-glance dashboard of their current elevation and pace -- without the complexity, battery drain, or social noise of full-featured GPS tracking apps.

The app records altitude and speed data points automatically when open, stores them locally in Room, provides 2-hour trend charts for recent activity, and offers optional full-data backup and restore to Cloudflare D1. There are no user accounts, no social feeds, no maps -- just accurate sensor data, clearly presented.

### 1.2 Target Users

| Persona | Context | Primary Need |
|---------|---------|--------------|
| **Weekend Hiker** | Hikes 1-4x/month, mid-range Android phone | Real-time altitude and speed, simple session logging |
| **Daily Walker** | Walks for exercise, tracks pace over time | Speed tracking + trend visualization |
| **Leisure Cyclist** | Casual bike rides, no full cycling computer | Speed readout + elevation change per ride |
| **Trail Runner** | Runs on trails, cares about elevation gain | Altitude accuracy + speed during varied terrain |
| **Peak Bagger** | Summits mountains, needs accurate elevation | Barometer-assisted altitude for precision |

### 1.3 Problem Statement

Most outdoor tracking apps are designed around route mapping and social sharing. Users who only care about real-time altitude and speed are forced to navigate cluttered UIs, accept heavy battery drain from continuous background GPS, and create accounts they do not need. The core pain points:

- **No fast, readable altitude display**: Existing apps bury elevation in secondary panels or require full session lifecycle management.
- **Speed jitter at low velocity**: Raw GPS speed oscillates around zero when standing still, creating confusion.
- **Outdoor readability**: Many apps use themes or fonts unreadable in direct sunlight.
- **Data portability**: Users who want their data backed up face account walls or proprietary lock-in.

---

## 2. Goals & Success Metrics

| Goal | Metric | Target | Measurement Window |
|------|--------|--------|--------------------|
| Core usability | Users who record at least one data point on first open | 90%+ | First 7 days |
| Altitude accuracy | Error vs. known reference points | +/- 15m (GPS only), +/- 8m (with barometer) | Controlled test |
| Speed stability | No jitter when stationary (speed < 1 km/h) | 0 false-positive readings | All conditions |
| Sync reliability | Backup/restore completes without data loss | 100% (0 corruption) | All sync operations |
| Battery efficiency | 30-min active session drain | < 8% | Mid-range device |
| App stability | Crash-free rate | > 99% | Post-launch |
| Engagement | % of active users viewing trends weekly | > 40% | 60 days post-launch |

---

## 3. Non-Goals (V1)

- **Background continuous recording** via Foreground Service -- V1 records only while the app is in the foreground.
- **Map / route tracking** -- No map display or GPS track rendering.
- **Social features** -- No sharing, feeds, leaderboards, or user profiles.
- **iOS version** -- Android-only.
- **User account system** -- No login. D1 sync uses a device-generated key.
- **Heart rate, cadence, or other sensor data** -- Only altitude and speed.
- **Export to GPX/TCX/KML** -- V2 consideration.
- **Wear OS companion** -- Phone-only.
- **Session history browser beyond 2 hours** -- V1 trends cover recent 2 hours only.

---

## 4. User Stories & Acceptance Criteria

### Story 1: Real-Time Altitude Display with Barometer Assist

**As a hiker, I want to see my current altitude in real time, using GPS and barometric pressure, so that I get the most accurate elevation reading possible.**

**Acceptance Criteria**:
- [ ] Altitude is displayed in meters (feet option in settings) in large, high-contrast text.
- [ ] Altitude updates at least once per second when GPS is active.
- [ ] When a barometric sensor is available, altitude is corrected using pressure data for improved accuracy.
- [ ] When barometric sensor is unavailable, the app falls back to GPS-only altitude and shows a small "GPS only" indicator.
- [ ] When GPS signal is lost, display shows "No GPS Signal" with the last known reading grayed out.
- [ ] Display is readable in direct sunlight (light theme, high-contrast, minimum 48sp primary text).

### Story 2: Real-Time Speedometer with Jitter Filter

**As a walker, I want to see my current speed without false fluctuations, so that I can trust the reading even when standing still.**

**Acceptance Criteria**:
- [ ] Speed is displayed in km/h (mph option in settings) in large text.
- [ ] Speed updates at least once per second when GPS is active.
- [ ] Speed readings below 1 km/h are filtered to 0 (eliminates GPS jitter when stationary).
- [ ] Speed does not jump abruptly between readings (3-sample moving average applied at display layer).
- [ ] When GPS signal is lost, speed displays 0 with "No GPS Signal" indicator.
- [ ] Display is readable in direct sunlight.

### Story 3: Combined Instrument Dashboard (Tab 1)

**As a user, I want altitude and speed on the same screen so I don't have to switch views during my activity.**

**Acceptance Criteria**:
- [ ] Tab 1 ("Dashboard") shows both altitude (primary, larger font) and speed (secondary) simultaneously.
- [ ] Both readings update independently in real time.
- [ ] Altitude has visual prominence (larger font size, accent color).
- [ ] Current activity type and location (if set) are displayed on the dashboard.
- [ ] Bottom tab bar clearly indicates the active tab.
- [ ] GPS signal state indicator is visible (green/yellow/red/gray).

### Story 4: Activity Type Selection (5 Types)

**As a user, I want to select from 5 activity types so that I can categorize my recorded data.**

**Acceptance Criteria**:
- [ ] 5 preset types: Hiking (default), Walking, Cycling, Running, Mountaineering.
- [ ] Activity type defaults to "Hiking" on first launch and subsequent opens.
- [ ] User can switch type at any time via the Settings screen.
- [ ] Changing type does not discard already-recorded data points.
- [ ] The selected type is stored with every data point recorded after the change.

### Story 5: Activity Location Tagging

**As a user, I want to optionally name my activity location so I can identify where each session happened.**

**Acceptance Criteria**:
- [ ] A location text field is available in Settings (optional, defaults to empty).
- [ ] User can type a free-text location name (max 200 characters).
- [ ] Location is stored with session data points recorded while that location is set.
- [ ] Location persists across app restarts until manually changed.
- [ ] Empty location is valid and means "no location tagged."

### Story 6: Trend Charts -- Past 2 Hours (Tab 2)

**As a user, I want to see altitude and speed trend lines for the past 2 hours so I can review my recent activity patterns.**

**Acceptance Criteria**:
- [ ] Tab 2 ("Trends") displays two charts: altitude vs. time and speed vs. time.
- [ ] Each chart covers the most recent 2 hours of recorded data.
- [ ] Charts update in near-real-time as new data points arrive (debounced, max 1 refresh/second).
- [ ] Y-axis auto-scales to visible data range with sensible padding.
- [ ] X-axis shows time labels (e.g., "10:30", "11:00", "11:30").
- [ ] Charts render smoothly with 7,200+ data points (2h at 1Hz) via downsampling.
- [ ] When no data exists in the 2-hour window, a clear empty state is shown.

### Story 7: Automatic Data Recording

**As a user, I want my altitude and speed data recorded automatically so I can review trends without manual intervention.**

**Acceptance Criteria**:
- [ ] Data recording starts automatically when the app opens and GPS acquires a fix.
- [ ] One data point per second while GPS is active and app is in foreground.
- [ ] Each data point includes: altitude, speed, type, location (nullable), timestamp.
- [ ] Data is persisted to Room immediately (no batching that risks data loss on kill).
- [ ] When the app is backgrounded, recording pauses.
- [ ] When the app returns to foreground, recording resumes automatically.
- [ ] Room uses WAL mode for safe concurrent reads during writes.

### Story 8: Cloud Backup to Cloudflare D1

**As a user, I want to back up all my data to the cloud so I don't lose it if my phone is lost or reset.**

**Acceptance Criteria**:
- [ ] "Sync & Backup" action is available in Settings.
- [ ] Tapping backup reads all local Room records and uploads to Cloudflare D1 via Workers API.
- [ ] A progress indicator (progress bar + percentage) is shown during upload.
- [ ] On completion, a toast/snackbar shows success with record count, or error on failure.
- [ ] Backup is idempotent -- running it twice produces no duplicates (upsert by timestamp).
- [ ] Backup works over both Wi-Fi and mobile data.
- [ ] If network is unavailable, "No network connection" error is shown before attempting upload.

### Story 9: Data Restore from Cloudflare D1

**As a user, I want to restore my data from the cloud onto a new or reset device.**

**Acceptance Criteria**:
- [ ] "Restore Data" action is available in Settings.
- [ ] Tapping restore fetches all records from Cloudflare D1 via Workers API.
- [ ] Fetched records are merged into local Room using INSERT OR IGNORE (by timestamp).
- [ ] A progress indicator is shown during download and merge.
- [ ] On completion, a toast/snackbar shows success with total local record count.
- [ ] Restore is safe to run multiple times (idempotent merge, never overwrites or deletes local data).
- [ ] If network is unavailable, a clear error message is shown.

### Story 10: Settings Screen Access

**As a user, I want to access all settings from the main screen so I can configure the app easily.**

**Acceptance Criteria**:
- [ ] A gear icon is visible in the top-right corner of both Tab 1 and Tab 2.
- [ ] Tapping the gear icon navigates to the Settings screen.
- [ ] Settings includes: Activity Type selector (5 types), Location input, Sync & Backup, Restore Data, Altitude Unit (meters/feet), Speed Unit (km/h/mph).
- [ ] Settings screen has back/up navigation to return to previous tab.
- [ ] Unit preference changes take effect immediately on the dashboard.

### Story 11: Large Dataset Sync Performance

**As a power user with months of data, I want backup and restore to handle large datasets without freezing the app.**

**Acceptance Criteria**:
- [ ] Backup and restore run on background coroutines -- UI remains responsive.
- [ ] Upload/download is paginated in 5,000-record chunks.
- [ ] No ANR even with 100,000+ local records.
- [ ] Memory stays below 100 MB during sync.

### Story 12: GPS Signal Degradation Handling

**As a user in a mountain valley or forest, I want the app to handle weak GPS gracefully.**

**Acceptance Criteria**:
- [ ] When GPS accuracy drops below 20m, a "Weak GPS" indicator appears (amber).
- [ ] When accuracy exceeds 50m, a "Poor GPS" indicator appears (red).
- [ ] When GPS is completely lost, altitude shows last known value (grayed) and speed shows 0.
- [ ] When GPS returns, readings resume normally without app restart.
- [ ] The app does not crash or hang during prolonged GPS signal loss.

---

## 5. Data Model

### 5.1 Room Database: Single Table `activity_record`

The V1 data model uses a **single flat table** for simplicity. No session abstraction -- every row is one data point.

| Column | Type | Nullable | Constraints | Description |
|--------|------|----------|-------------|-------------|
| `id` | `Long` | No | PRIMARY KEY AUTOINCREMENT | Row identifier |
| `altitude` | `Double` | No | -- | Altitude in meters (SI). Barometer-corrected when available. |
| `speed` | `Double` | No | -- | Speed in meters per second (SI). Values < 0.278 (1 km/h) stored as 0.0. |
| `type` | `String` | No | CHECK(type IN ('HIKING','WALKING','CYCLING','RUNNING','MOUNTAINEERING')) | Activity type |
| `location` | `String` | Yes | -- | User-entered location name. NULL = not set. Max 200 chars. |
| `timestamp` | `Long` | No | UNIQUE | Unix epoch milliseconds (UTC). Unique for idempotent sync. |

**Index**: `CREATE INDEX idx_timestamp ON activity_record(timestamp)` -- efficient range queries for 2-hour trend charts.

### 5.2 Storage Conventions

- **All values in SI units**: altitude in meters, speed in m/s. Display layer handles unit conversion.
  - Altitude: 1 m = 3.28084 ft
  - Speed: 1 m/s = 3.6 km/h = 2.23694 mph
- **Timestamps**: Unix epoch milliseconds, UTC. Display converts to local timezone.
- **Speed floor**: Raw GPS speed < 0.278 m/s (1 km/h) is stored as 0.0. Display shows "0" or "0.0 km/h".
- **Speed smoothing**: Display-layer applies 3-sample moving average. Stored value is raw (floored) GPS speed.

### 5.3 Recording Rules

| Rule | Detail |
|------|--------|
| Recording rate | 1 point/second while GPS active and app in foreground |
| Speed floor | < 0.278 m/s stored as 0.0 |
| Altitude source priority | Barometer-assisted > GPS-only > last known |
| Background behavior | Recording pauses (no foreground service in V1) |
| Estimated storage | ~50 bytes/record. 1h = ~180 KB. 1 month daily 1h = ~5.4 MB. |

### 5.4 Database Migration Strategy

- Room schema versioned starting at v1.
- All schema changes must be **additive only** (add columns with defaults, add indexes). Never drop, rename, or change column types.
- Each version change requires a `Migration` class with explicit `ALTER TABLE` SQL.
- `fallbackToDestructiveMigration` is **disabled** -- if migration fails, the app must not silently delete user data.
- Migrations are tested against a populated database to verify zero data loss.

---

## 6. Sync Architecture (Cloudflare D1)

### 6.1 Overview

Sync uses a **full-dataset upload/download** model. Each device is identified by a locally generated UUID (`device_key`). The Cloudflare Workers API handles backup and restore operations against D1.

### 6.2 Authentication

- On first launch, the app generates a UUID stored in `EncryptedSharedPreferences`.
- Every API request includes `X-Device-Key: <uuid>` header.
- No user account, no password, no OAuth. The device key is both identifier and auth token.
- **Security note**: Device key is not strong auth. Sufficient for personal backup/restore where the primary threat model is device loss, not malicious access.

### 6.3 Backup Flow

```
User taps "Sync & Backup"
    |
    v
App reads all records from Room (paginated, 5000 per page)
    |
    v
For each page: POST /api/sync
  Headers: X-Device-Key: <uuid>
  Body: { records: [{altitude, speed, type, location, timestamp}, ...] }
    |
    v
Worker: INSERT INTO activity_record ... ON CONFLICT(timestamp) DO UPDATE SET ...
    |
    v
Returns: { status: "ok", upserted: N }
    |
    v
App updates progress bar. Shows success toast with total count on completion.
```

### 6.4 Restore Flow

```
User taps "Restore Data"
    |
    v
App calls GET /api/restore (paginated, 5000 per page)
  Headers: X-Device-Key: <uuid>
    |
    v
Worker: SELECT * FROM activity_record WHERE device_key = ? ORDER BY timestamp
    |
    v
Returns: { records: [...], total: N }
    |
    v
App merges each page into Room via DAO.insertOrIgnore(records)
  (INSERT OR IGNORE -- by timestamp unique constraint)
    |
    v
Shows success toast with total local record count.
```

### 6.5 Large Dataset Handling (100k+ Records)

| Concern | Approach |
|---------|----------|
| Pagination | 5,000 records per chunk, both upload and download |
| Progress UI | "Backing up... X / Y records" with progress bar |
| Timeout | 30 seconds per API call. Retry 3x with exponential backoff (1s, 2s, 4s) |
| Failed chunk | Skip and report in final summary. User can re-run. |
| Memory | Stream from Room via Flow. Stream from server via `JsonReader`. Never load full dataset into a single `List`. |
| ANR prevention | All sync work on `Dispatchers.IO`. UI remains responsive. |

### 6.6 Offline Behavior

- Backup/Restore check `ConnectivityManager` before any API call.
- If offline: `Snackbar` with "No network connection. Try again later."
- No automatic retry queue in V1. User manually retries.
- Offline state has zero impact on local recording or display.

---

## 7. GPS & Sensor Handling

### 7.1 Altitude Sources

| Source | Accuracy | Availability | Behavior |
|--------|----------|-------------|----------|
| GPS altitude | +/- 15m (open sky) | All Android phones | Primary source |
| Barometric pressure | +/- 3-5m (after calibration) | Phones with barometer (Pixel, Galaxy S, many mid-range) | Fused with GPS |
| GPS only (fallback) | +/- 15m | Always | Used when barometer unavailable |

**Fusion approach**: Use `SensorManager` to read `TYPE_PRESSURE`. Convert pressure to altitude via hypsometric formula with sea-level reference obtained from GPS on first fix. Apply weighted average: 60% barometer, 40% GPS, adjustable based on GPS accuracy estimate.

**Barometer unavailability**: At startup, detect via `SensorManager.getDefaultSensor(TYPE_PRESSURE)`. If null, use GPS-only altitude. No error message needed -- app works identically, just with lower altitude accuracy.

### 7.2 Speed Calculation

- **Source**: `FusedLocationProviderClient.getSpeed()` (m/s), derived from GPS position deltas by the system.
- **Jitter filter**: Readings < 1 km/h (0.278 m/s) clamped to 0.
- **Smoothing**: Display-layer 3-sample moving average. Raw (floored) value stored in DB.

### 7.3 GPS Signal States

| State | Accuracy | Display | Indicator |
|-------|----------|---------|-----------|
| Active | < 20m | Normal readings | Green circle, "GPS Active" |
| Weak | 20-50m | Readings continue | Amber circle, "Weak GPS" |
| Poor | > 50m | Readings continue | Red circle, "Poor GPS" |
| Lost | No fix | Last altitude grayed, speed = 0 | Gray X, "No GPS Signal" |

**Recovery**: When GPS signal returns, display resumes with new values. No app restart needed.

---

## 8. Non-Functional Requirements

### 8.1 Performance

| Scenario | Requirement |
|----------|-------------|
| Cold start to interactive | < 2 seconds |
| GPS to display latency | < 500ms |
| Trend chart render (7200 points) | < 200ms |
| Sync (100k records) | No ANR, UI responsive |
| Peak memory | < 100 MB |

### 8.2 Outdoor Readability

- **Font size**: Primary readings (altitude) minimum 48sp. Secondary (speed) minimum 36sp.
- **Contrast**: Text on background meets WCAG AA (4.5:1 minimum).
- **Touch targets**: Minimum 48dp x 48dp for gloved operation.
- **Theme**: Light theme default, optimized for outdoor sunlight readability (high-contrast text on light background reduces screen glare).
- **Screen wake**: Use `FLAG_KEEP_SCREEN_ON` (or `keepScreenOn = true` in Compose) while recording to prevent screen timeout during active use.

### 8.3 Battery

- GPS polling: 1-second interval, `PRIORITY_HIGH_ACCURACY`.
- GPS listeners released when app is backgrounded.
- No foreground service in V1.
- Target: < 8% battery drain over 30-minute active session on mid-range device.

### 8.4 Stability

- Crash-free rate: > 99%.
- ANR rate: < 0.5%.
- No data corruption on crash/kill (Room WAL mode).
- Database migration: additive only, `fallbackToDestructiveMigration = false`.

---

## 9. Technical Constraints

| Constraint | Detail |
|------------|--------|
| Platform | Android API 26+ (Android 8.0 Oreo), covers 95%+ active devices |
| Target SDK | API 35 (Android 15) |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + Repository + Room DAO) |
| Local DB | Room (WAL mode) |
| Remote DB | Cloudflare D1 via Workers REST API |
| Navigation | Jetpack Navigation Component |
| DI | Hilt |
| Async | Kotlin Coroutines + Flow |
| Charts | Vico or YCharts (Compose-native charting) |
| Build | Gradle (Kotlin DSL), R8 for release |
| CI/CD | GitHub Actions (build + lint + unit tests on PR) |

---

## 10. V1 MVP Scope

### P0 -- Must Have for Launch

| Feature | Story |
|---------|-------|
| Real-time altitude (GPS + barometer fallback) | Story 1 |
| Real-time speed (GPS, jitter filtered) | Story 2 |
| Dashboard (Tab 1) -- altitude + speed | Story 3 |
| Activity type selector (5 types) | Story 4 |
| Location tagging (optional, free-text) | Story 5 |
| Auto-recording to Room (foreground only) | Story 7 |
| Trend charts (Tab 2) -- 2h altitude + speed | Story 6 |
| Settings screen (gear icon) | Story 10 |
| Unit preferences (meters/feet, km/h/mph) | Story 10 |
| GPS signal state indicators | Story 12 |
| Edge cases (GPS loss, barometer unavailable, no network) | Stories 10, 12 |

### P1 -- Should Have for Launch

| Feature | Story |
|---------|-------|
| Cloud backup (Cloudflare D1) | Story 8 |
| Data restore (Cloudflare D1) | Story 9 |
| Large dataset sync (100k+ records, paginated) | Story 11 |

### Deferred to V2+

| Feature | Why |
|---------|-----|
| Background recording (Foreground Service) | Battery trade-off needs user validation |
| Map / GPS track | Requires map SDK, significant scope |
| History browser (beyond 2 hours) | V1 validates trend chart UX first |
| GPX/TCX export | Power user feature, low V1 demand |
| Strava / fitness app integration | Third-party dependency |
| Account system + multi-device sync | V1 device-key is simpler |
| Data export (CSV/JSON) | Nice-to-have |
| Wear OS companion | Platform expansion |
| Voice alerts / milestones | Enhancement |
| Widget / always-on display | Enhancement |

---

## 11. UI Structure

### Tab 1 -- Dashboard

```
+------------------------------------------+
| HikingHappy                    [Gear]    |
|                                          |
|            1,247.3 m                     |  Altitude (primary, 48sp+)
|           ALTITUDE                  [i]  |  [i] = GPS only indicator (when baro N/A)
|                                          |
|     [Hiking|Walk|Cycling|Run|Climb]      |  Current activity type
|     Location: _Yosemite Valley_          |  Current location (if set)
|                                          |
|               5.2 km/h                   |  Speed (secondary, 36sp)
|              SPEED                       |
|                                          |
|           GPS: Active                    |  Signal state indicator
+------------------------------------------+
|   [Dashboard]         [Trends]           |  Bottom tab bar
+------------------------------------------+
```

### Tab 2 -- Trends

```
+------------------------------------------+
| HikingHappy                    [Gear]    |
|                                          |
|   Altitude -- Last 2 Hours               |
|   +----------------------------------+   |
|   |        /\         /\              |   |
|   |   /\  /  \   /\  /  \    /\      |   |
|   |  /  \/    \ /  \/    \  /  \     |   |
|   +----------------------------------+   |
|   11:00        11:30        12:00       |
|                                          |
|   Speed -- Last 2 Hours                  |
|   +----------------------------------+   |
|   |   _______________                  |   |
|   |  /               \___              |   |
|   | /                    \__           |   |
|   +----------------------------------+   |
|   11:00        11:30        12:00       |
|                                          |
|          (empty state if no data)        |
+------------------------------------------+
|   [Dashboard]         [Trends]           |
+------------------------------------------+
```

### Settings Screen

```
+------------------------------------------+
| < Back                Settings           |
|                                          |
|  Activity Type                           |
|  [  Hiking  v  ]                         |
|                                          |
|  Location                                |
|  [  ________________________  ]          |
|                                          |
|  Units                                   |
|  Altitude:  [Meters] [Feet]              |
|  Speed:     [km/h]   [mph]               |
|                                          |
|  Data & Sync                             |
|  [  Sync & Backup  ]                     |
|  Last backup: 2026-04-05 14:32           |
|                                          |
|  [  Restore Data  ]                      |
|                                          |
+------------------------------------------+
```

### GPS Signal Indicator States

| State | Icon Color | Label |
|-------|-----------|-------|
| Active (< 20m) | Green `#4ECB71` | "GPS Active" |
| Weak (20-50m) | Amber `#F0A500` | "Weak GPS" |
| Poor (> 50m) | Red `#E74C3C` | "Poor GPS" |
| Lost | Gray `#6B7280` | "No GPS Signal" |

### Color Palette (Light Theme, Material 3)

| Role | Color | Hex |
|------|-------|-----|
| Background | White | `#FFFFFF` |
| Surface | Light gray | `#F8F9FA` |
| Surface variant | Card background | `#F0F2F5` |
| Primary text | Near black | `#1A1A2E` |
| Secondary text | Medium gray | `#6B7280` |
| Altitude accent | Mountain green | `#22944A` (darkened for light bg contrast) |
| Speed accent | Sky blue | `#2563EB` (darkened for light bg contrast) |
| Active tab underline | Accent | `#22944A` |
| Error / GPS lost | Amber / Red | `#D97706` / `#DC2626` |

---

## 12. Edge Cases & Boundary Scenarios

### 12.1 GPS Signal Weak / Lost

User enters a dense forest, canyon, or tunnel where GPS drops.

- Altitude shows last known value (grayed) with "No GPS Signal" indicator.
- Speed shows 0.
- Intermediate degradation: "Weak GPS" (amber, 20-50m) and "Poor GPS" (red, >50m) states.
- Recording pauses (no GPS data to record). Resumes automatically on signal return.
- App does not crash or hang. No special recovery action needed.

### 12.2 Barometer Unavailable

Device lacks a pressure sensor (e.g., budget phone).

- Detected at startup via `SensorManager.getDefaultSensor(TYPE_PRESSURE)`.
- Altitude falls back to GPS-only. Small "GPS only" label shown next to altitude.
- No error message. App works identically with lower altitude accuracy (+/- 15m vs +/- 8m).

### 12.3 No Network During Backup/Restore

User taps Backup or Restore with no internet.

- Check `ConnectivityManager.activeNetwork` before any API call.
- If no network: `Snackbar` -- "No network connection. Try again later."
- No automatic retry. User retries manually.
- Zero impact on local recording or display.

### 12.4 Large Dataset Sync (100k+ Records)

User has accumulated massive local data and attempts sync.

- Paginated in 5,000-record chunks.
- Progress bar updates per chunk.
- Each API call: 30s timeout, 3x retry with exponential backoff.
- Failed chunks skipped and reported in summary.
- Streaming from Room (Flow) and from server (JsonReader) -- no full-dataset-in-memory.
- No ANR. Memory stays under 100 MB.

### 12.5 Database Migration Compatibility

Future app update changes Room schema.

- All changes additive only (add columns with defaults, add indexes).
- Never drop, rename, or change existing column types.
- Explicit `Migration` class per version bump.
- `fallbackToDestructiveMigration = false`.
- Tested against populated database.

### 12.6 App Killed During Use

Android kills the app while in foreground (or user swipes it away).

- Room WAL mode ensures all committed writes survive.
- At most 1 second of data lost (current uncommitted point).
- No foreground service in V1, so Android may kill freely. This is accepted.

### 12.7 Device Rotation / Config Change

User rotates phone during use.

- Compose handles configuration changes natively. ViewModel survives.
- Active recording state preserved. No data loss.
- Charts and dashboard re-render with current data.

---

## 13. Launch Plan

| Phase | Target Date | Audience | Success Gate |
|-------|-------------|----------|-------------|
| Internal alpha | 2026-04-18 | Team + 3 beta testers | P0 features complete, no P0 crashes, GPS accuracy validated |
| Closed beta | 2026-04-28 | 20 opted-in testers | P0 + P1 complete, crash rate < 2%, no data loss in sync |
| GA (Play Store) | 2026-05-12 | Public | 0 P0 bugs open, crash rate < 1%, all acceptance criteria met |

**Rollback criteria**: If crash-free rate drops below 98% in first 48 hours of GA, pull the release and investigate.

---

## 14. Open Questions

| # | Question | Owner | Deadline |
|---|----------|-------|----------|
| 1 | Cloudflare D1 schema provisioning and Workers API deployment | Engineering | 2026-04-08 |
| 2 | API key generation and secure storage approach | Engineering | 2026-04-08 |
| 3 | Barometer availability test matrix (which devices) | QA | 2026-04-10 |
| 4 | Play Store developer account setup | PM | 2026-04-10 |
| 5 | App icon and store listing assets | Design | 2026-04-12 |
| 6 | Chart library final decision (Vico vs YCharts) | Engineering | 2026-04-09 |

---

## 15. Appendix

### A. Competitive Landscape

| App | Altitude | Speed | Barometer | Trends | Backup | Ad-Free | Notes |
|-----|----------|-------|-----------|--------|--------|---------|-------|
| HikingHappy | Yes | Yes | Yes | Yes (2h) | Yes | Yes | Focused, accurate, clean |
| Strava | Partial | Yes | No | Yes | Yes | No | Overkill for simple use |
| Komoot | Partial | Yes | No | Partial | Yes | No | Route-focused |
| My Altitude | Yes | No | No | No | No | No | Ad-heavy |
| Barometer Plus | Yes | No | Yes | No | No | No | Speed missing |
| GPS Speedometer | No | Yes | No | No | No | Partial | Altitude missing |

### B. Key Technical References

- Android `FusedLocationProviderClient` -- `getSpeed()`, `getAltitude()`, `getAccuracy()`
- Android `SensorManager` -- `TYPE_PRESSURE` for barometric altitude
- Hypsometric formula: `h = 44330 * (1 - (P/P0)^(1/5.255))`
- Room `@Insert(onConflict = OnConflictStrategy.IGNORE)` for idempotent merge
- Cloudflare D1 REST API -- `INSERT ... ON CONFLICT` for upsert
- Kotlin Coroutines + Flow for reactive GPS data streams

### C. Glossary

| Term | Definition |
|------|-----------|
| Barometer-assisted altitude | Altitude from atmospheric pressure, calibrated against GPS. More accurate for small elevation changes. |
| GPS jitter | Small apparent movement from GPS signal noise when stationary. Manifests as non-zero speed readings. |
| Hypsometric formula | Mathematical relationship between pressure and altitude. `h = 44330 * (1 - (P/P0)^(1/5.255))`. |
| WAL mode | Write-Ahead Logging. SQLite journaling mode enabling concurrent reads during writes. |
| Device key | UUID generated on first launch, used as both device identifier and sync auth token. |
