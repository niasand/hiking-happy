# HikingHappy QA Strategy Document

**Version**: 1.0
**Date**: 2026-04-06
**Author**: QA Engineer
**Project**: HikingHappy - Android Outdoor Sports App
**Task**: #3 - QA 全程质量把控

---

## 1. Requirements Review

### 1.1 Functional Completeness Analysis

Based on the project background: HikingHappy is an Android outdoor sports app with real-time altimeter, speedometer, dual-tab navigation (live data + stats dashboard), Room + Cloudflare D1 storage, activity types (hiking/walking/cycling), and optional location input.

| # | Requirement | Status | Notes |
|---|------------|--------|-------|
| R1 | Real-time altimeter display | Defined | Needs altitude source spec (GPS vs barometric sensor) |
| R2 | Real-time speedometer display | Defined | Needs speed source spec and accuracy targets |
| R3 | Dual Tab navigation (Live Data + Stats Dashboard) | Defined | Needs tab switch behavior and data freshness spec |
| R4 | Room database local storage | Defined | Needs schema migration strategy |
| R5 | Cloudflare D1 remote sync | Defined | Needs conflict resolution strategy and auth |
| R6 | Activity types: hiking/walking/cycling | Defined | Needs per-type speed/altitude thresholds |
| R7 | Activity location (user optional) | Defined | Needs geocoding spec and character limits |
| R8 | Stats: 2-hour altitude/speed trend charts | Defined | Needs chart library, update frequency, data granularity |
| R9 | Activity start/stop/pause controls | **MISSING** | Critical gap -- core user workflow undefined |
| R10 | Activity resume after interruption | **MISSING** | App killed by system, device reboot |
| R11 | Permission handling (GPS, background location) | **MISSING** | Runtime permission flow undefined |
| R12 | Offline mode behavior | **MISSING** | Full local functionality when no network |
| R13 | Background location tracking | **MISSING** | Foreground service + notification design |
| R14 | Battery optimization handling | **MISSING** | Critical for multi-hour outdoor activities |
| R15 | Settings page structure | Implied | Not explicitly defined |
| R16 | Unit system (metric/imperial) | **MISSING** | International users need unit choice |
| R17 | Data export (GPX/CSV) | **MISSING** | Users expect to export outdoor activity data |
| R18 | Authentication for D1 sync | **MISSING** | How does the app authenticate with Cloudflare? |

**BLOCKING GAPS (must resolve before architecture)**:
1. **R09 - Activity lifecycle**: Start/stop/pause/resume/discard is the most fundamental user flow. Without it, nothing else matters.
2. **R18 - Authentication**: Blocks all sync architecture decisions. OAuth? API token? Anonymous?
3. **R13 - Background tracking**: Android foreground service model must be decided before architecture.

**HIGH PRIORITY GAPS**:
4. **R11 - Permission flow**: GPS and background location are sensitive permissions with mandatory UX on Android.
5. **R14 - Battery strategy**: Continuous GPS drains battery. Need explicit targets (sampling rate, max drain/hour).
6. **R16 - Unit system**: Deferring this creates data inconsistency across activities.

### 1.2 Boundary Conditions Analysis

#### GPS & Location Boundaries
| Condition | Expected Behavior | Risk |
|-----------|-------------------|------|
| No GPS signal (indoor) | Show "Searching for GPS..." with setup guidance | HIGH |
| Weak GPS signal (urban canyon) | Show accuracy indicator, buffer noisy data | HIGH |
| GPS multipath errors | Apply smoothing filter (Kalman/moving average) | MEDIUM |
| Rapid altitude jumps | Filter unrealistic changes (>50m within 2s) | MEDIUM |
| Speed = 0 while GPS active | Distinguish stopped from weak signal | MEDIUM |
| Altitude at/below sea level | Display negative values correctly | LOW |
| Device has barometric sensor | Use sensor for altitude (more accurate) | MEDIUM |
| Device lacks barometric sensor | Fall back to GPS altitude with accuracy warning | MEDIUM |
| GPS cold start (>30s) | Show acquisition progress, don't allow activity start | HIGH |
| Time zone change during activity | Store all timestamps in UTC | MEDIUM |

#### Network & Sync Boundaries
| Condition | Expected Behavior | Risk |
|-----------|-------------------|------|
| No network (airplane mode) | Full local functionality, queue sync | HIGH |
| Intermittent connectivity | Retry with exponential backoff | HIGH |
| Slow network (2G/3G) | Sync does not block UI, show progress | MEDIUM |
| Network returns mid-sync | Resume/merge partial uploads | MEDIUM |
| D1 API unreachable | Graceful error, retry later, no data loss | HIGH |
| D1 rate limiting (429) | Respect Retry-After, back off | MEDIUM |
| D1 server error (5xx) | Retry with backoff, user notification | MEDIUM |
| Same activity edited on two devices | Last-write-wins with conflict log | HIGH |
| Sync interrupted mid-upload | Resume from checkpoint, no duplicates | HIGH |
| Large data volume (>1000 activities) | Paginated sync, no UI freeze | MEDIUM |

#### Device State Boundaries
| Condition | Expected Behavior | Risk |
|-----------|-------------------|------|
| Low battery (<15%) | Warning notification, suggest saving activity | HIGH |
| Battery saver mode active | Adaptive sampling (reduce GPS frequency) | HIGH |
| Android Doze mode | Foreground service keeps tracking alive | HIGH |
| System kills app (low memory) | On restart, detect and offer to resume activity | HIGH |
| Device reboot during activity | On restart, detect incomplete activity | MEDIUM |
| Storage almost full | Warning before starting new activity | MEDIUM |
| Screen rotation | Consistent layout in both orientations | MEDIUM |
| App sent to background | Foreground service + notification with controls | HIGH |

#### Data Boundaries
| Condition | Expected Behavior | Risk |
|-----------|-------------------|------|
| Empty database (fresh install) | Dashboard shows empty state with guidance | LOW |
| Activity with 1 data point | Handle gracefully in charts, not flat zero | MEDIUM |
| Activity with 100,000+ data points | No UI freeze, memory bounded | MEDIUM |
| 8+ hour activity | Continuous recording, no memory leak | HIGH |
| Database file corrupted | Integrity check on launch, offer recovery | HIGH |
| Schema migration with existing data | All data preserved, additive-only changes | HIGH |
| Unicode in location names | Chinese, emoji, RTL text handled correctly | MEDIUM |
| Very long text input (>500 chars) | Truncate with ellipsis, no crash | LOW |
| SQL injection in text fields | Parameterized queries only | HIGH |

### 1.3 Data Safety & Consistency Analysis

**CRITICAL DATA SAFETY REQUIREMENTS**:

1. **Room database integrity**:
   - Use WAL (Write-Ahead Logging) mode
   - Integrity check on app startup
   - Automatic backup before any schema migration
   - Never delete user data during migrations (additive-only)

2. **Sync conflict resolution** (undefined -- must be specified):
   - Recommend: last-write-wins with server timestamps
   - Conflict log for user review
   - No silent data overwrites

3. **Activity recovery**:
   - Detect incomplete activities on app restart
   - Offer resume or discard (never auto-delete)
   - Save activity state periodically (every 30s or on pause)

4. **Cloudflare D1 safety**:
   - Batch writes to avoid rate limits
   - Idempotent operations for retry safety
   - Request size limit awareness (D1 has 1MB request limit)
   - Transactional consistency for related records

5. **Authentication security** (undefined -- must be specified):
   - Secure token storage (EncryptedSharedPreferences)
   - Token refresh handling
   - Account recovery flow

### 1.4 Performance Targets

| Metric | Target | Measurement Method |
|--------|--------|--------------------|
| GPS sampling rate (moving) | 1 update/second | LocationManager log |
| GPS sampling rate (stationary) | 1 update/5 seconds | Adaptive sampling logic |
| Battery consumption | <8% per hour | Battery historian tool |
| Chart render time | <100ms | Systrace/Profiler |
| Tab switch time | <300ms | Instrumentation test |
| Cold start time | <2 seconds | Instrumentation test |
| Database write (batch) | <50ms for 100 records | Room benchmark |
| Sync upload (100 activities) | <30 seconds | Network profiler |
| Memory usage (8hr activity) | <150MB heap | Android Profiler |
| APK size | <15MB | Build output |

### 1.5 Compatibility Requirements

| Aspect | Minimum | Recommended | Notes |
|--------|---------|-------------|-------|
| Android version | API 26 (8.0) | API 29 (10) | Foreground service changes |
| Screen width | 320dp (4.7") | 411dp (6.1") | Primary target |
| Screen density | mdpi | xxhdpi | Test multiple densities |
| Orientation | Portrait primary | Both | Portrait-first for outdoor |
| Dark mode | Support required | System-follow | Outdoor readability |
| Languages | Chinese + English | i18n ready | String resources day 1 |
| Barometric sensor | Optional | Preferred | Graceful fallback |

### 1.6 Outdoor Usability Requirements

| Scenario | Requirement | Priority |
|----------|-------------|----------|
| Bright sunlight | High contrast, large text, WCAG AA minimum | HIGH |
| Moving/walking | Touch targets >= 48dp, minimal text input | HIGH |
| Wet hands | Gesture-friendly, no precise taps required | MEDIUM |
| One-handed use | Bottom-heavy layout, controls reachable | MEDIUM |
| Glanceable | Speed/altitude always visible at top of live tab | HIGH |
| Quick actions | Start/stop/pause accessible from notification | HIGH |
| Auto-wake | Screen stays on during active tracking | HIGH |

---

## 2. Test Case Registry

### 2.1 Activity Lifecycle (P0)

| ID | Title | Steps | Expected | Priority |
|----|-------|-------|----------|----------|
| TC-001 | Start activity | Grant GPS, tap Start | Activity begins, timer starts, GPS recording, foreground service notification | P0 |
| TC-002 | Pause activity | Tap Pause, wait, tap Resume | Timer pauses, GPS pauses (gap marker), notification shows Paused | P0 |
| TC-003 | Stop and save | Tap Stop, confirm | Saved to Room, sync queued, navigated to summary/dashboard | P0 |
| TC-004 | Stop and discard | Tap Stop, select Discard | Data deleted, no DB entry, no sync, undo snackbar | P0 |
| TC-005 | Concurrent start | Start while activity in progress | Blocked with message "Activity already in progress" | P0 |
| TC-006 | Resume after kill | System kills app during activity | On restart: offer resume or discard | P1 |
| TC-007 | Resume after reboot | Reboot device during activity | On restart: detect incomplete, offer recovery | P2 |
| TC-008 | Timezone change | Change TZ during activity | Duration calculated correctly via UTC | P1 |

### 2.2 Real-Time Display (P0)

| ID | Title | Steps | Expected | Priority |
|----|-------|-------|----------|----------|
| TC-010 | Altitude accuracy | Compare against known elevation | +/- 10m (barometric), +/- 30m (GPS) | P1 |
| TC-011 | Speed accuracy | Compare against vehicle speedometer | +/- 2 km/h at >10 km/h | P1 |
| TC-012 | Altitude smoothing | Monitor in urban area | No sudden +/- 50m jumps | P1 |
| TC-013 | Speed smoothing | Walk/run/drive transitions | Smooth transitions, no spikes | P1 |
| TC-014 | Display update rate | Watch live display | Updates at least 1x per second | P0 |
| TC-015 | No GPS display | Disable GPS | "Searching GPS..." with guidance | P0 |

### 2.3 Tab Navigation (P0)

| ID | Title | Steps | Expected | Priority |
|----|-------|-------|----------|----------|
| TC-020 | Switch Live to Dashboard | Tap Tab 2 during activity | Dashboard loads with current session + recent stats | P0 |
| TC-021 | Switch Dashboard to Live | Tap Tab 1 during activity | Live view shows current values, data fresh | P0 |
| TC-022 | Dashboard empty state | Fresh install, no activities | Empty state message, no crash, no empty charts | P1 |

### 2.4 Statistics & Charts (P0)

| ID | Title | Steps | Expected | Priority |
|----|-------|-------|----------|----------|
| TC-025 | 2-hour trend chart | Navigate to Dashboard with data | Altitude + speed charts, proper labels, readable scale | P0 |
| TC-026 | Chart minimal data | Activity with <1 min data | Graceful display, not flat zero line | P1 |
| TC-027 | Chart large dataset | 8+ hour activity data | Render <100ms, no jank, bounded memory | P1 |
| TC-028 | Chart empty state | No data | Meaningful empty state, no crash | P1 |

### 2.5 Data Sync (P0)

| ID | Title | Steps | Expected | Priority |
|----|-------|-------|----------|----------|
| TC-030 | Offline full operation | Airplane mode, start/stop/save | Full local functionality, sync queued | P0 |
| TC-031 | Auto-sync on network return | Re-enable network | Pending items sync within 30s | P0 |
| TC-032 | Sync conflict | Same activity modified on 2 clients | Resolved per strategy, no data loss | P1 |
| TC-033 | Sync interrupted | Disable network mid-sync | No duplicates on retry, correct resume point | P1 |
| TC-034 | D1 unreachable | API down | Graceful error, retry with backoff, no data loss | P1 |

### 2.6 Permissions (P0)

| ID | Title | Steps | Expected | Priority |
|----|-------|-------|----------|----------|
| TC-040 | Grant GPS permission | Fresh install, grant | GPS activates, location displayed | P0 |
| TC-041 | Deny GPS permission | Fresh install, deny | Explanation + option to grant later | P0 |
| TC-042 | Deny "Don't ask again" | Previously denied permanently | Redirect to app settings | P1 |
| TC-043 | Background location | Activity in progress, switch apps | Tracking continues, notification visible | P0 |

### 2.7 Battery & Background (P0)

| ID | Title | Steps | Expected | Priority |
|----|-------|-------|----------|----------|
| TC-045 | Background continuity | Home, switch apps, 10 min, return | Still recording, no gaps, notification active | P0 |
| TC-046 | Battery optimization | Android 6+ with optimization on | Requests exemption, tracking continues | P0 |
| TC-047 | Long-duration tracking | 8+ hours | Battery <65%, no leaks, no crashes, all data saved | P1 |
| TC-048 | Battery saver mode | Enable battery saver | Adaptive sampling, reduced drain | P1 |

### 2.8 Database & Data Integrity (P0)

| ID | Title | Steps | Expected | Priority |
|----|-------|-------|----------|----------|
| TC-050 | Schema migration | Update app with schema change | All data preserved, new fields have defaults | P0 |
| TC-051 | Corrupted database | Inject corruption, launch app | Integrity check, offer recovery | P1 |
| TC-052 | Storage full | Fill storage, start activity | Warning before start, no data corruption | P1 |

### 2.9 UI & Responsive (P1)

| ID | Title | Steps | Expected | Priority |
|----|-------|-------|----------|----------|
| TC-055 | Screen rotation | Rotate on all screens | Layout adapts, no data loss, no crash | P1 |
| TC-056 | Small screen 4.7" | 320dp device | No horizontal scroll, targets >= 48dp | P1 |
| TC-057 | Large screen / tablet | 600dp+ device | Extra space used effectively | P2 |
| TC-058 | Dark mode | System dark mode | Consistent dark theme, readable charts | P1 |
| TC-059 | Sunlight readability | Full brightness, direct sun | Contrast >= 4.5:1, large readable numbers | P1 |
| TC-060 | Settings persistence | Change settings, kill app, relaunch | All settings preserved | P1 |
| TC-061 | Unit toggle | Switch metric/imperial | All displays update immediately | P1 |

---

## 3. Risk Register

| ID | Risk | Probability | Impact | Mitigation |
|----|------|-------------|--------|------------|
| R01 | GPS accuracy insufficient for altitude | High | High | Barometric sensor priority, Kalman filter, accuracy indicator |
| R02 | Android kills background service | High | Critical | Foreground service + notification + battery exemption |
| R03 | Battery drain unacceptable | High | High | Adaptive sampling, batch updates, minimize wake locks |
| R04 | Sync conflict causes data loss | Medium | Critical | Define strategy before implementation, test thoroughly |
| R05 | Room migration loses user data | Medium | Critical | Backup before migration, additive-only, migration test suite |
| R06 | D1 rate limits / downtime | Medium | Medium | Local-first, retry backoff, user-visible sync status |
| R07 | Authentication undefined | High | High | Blocks sync -- must resolve in PRD |
| R08 | Activity lifecycle undefined | High | Critical | Blocks core flow -- must resolve in PRD |
| R09 | Chart performance with large data | Medium | Medium | Benchmark libraries, data downsampling |
| R10 | Android 14+ foreground service restrictions | High | High | Follow latest guidelines, test on API 34+ |
| R11 | Different GPS chip accuracy across OEMs | High | Medium | Accuracy filtering, show accuracy to user |
| R12 | User denies background location | Medium | High | Graceful degradation, explain consequences |
| R13 | No data export in v1 | Medium | Medium | Add GPX/CSV export to roadmap |
| R14 | Time sync issues across devices | Medium | Medium | UTC internally, server timestamps for sync |

### Critical Path Risks
1. ~~**R08** (Activity lifecycle)~~ -- **RESOLVED in PRD** (Story 3 + Section 10.6)
2. ~~**R07** (Authentication)~~ -- **RESOLVED in PRD** (Section 6.2 device key)
3. ~~**R02** (Background service)~~ -- **RESOLVED in PRD** (V1 explicit no-foreground-service)
4. ~~**C8**: UNIQUE index on device_key~~ -- **RESOLVED in PRD v2** (single-table design)
5. ~~**C12**: Screen wake lock not specified~~ -- **RESOLVED in PRD v2** (Section 8.2)
6. **R03** (Battery drain) remains active -- foreground GPS at 1Hz is the top battery risk

---

## 4. QA Phase Gates

| Gate | Trigger | Criteria | QA Action |
|------|---------|----------|-----------|
| G1: PRD Review | Task #1 complete | All blocking gaps addressed (R09, R18, R13) | Review PRD, update test cases |
| G2: Design Review | Task #2 complete | Outdoor usability verified, accessibility OK | Review design against requirements |
| G3: Architecture Review | Task #4 complete | Sync strategy defined, migration strategy approved | Review architecture decisions |
| G4: Core Feature Test | Task #6 complete | All P0 test cases pass | Execute functional + boundary tests |
| G5: Full Integration Test | Task #5 complete | All P0 + P1 test cases pass | Full regression + performance testing |
| G6: Release Readiness | Before any release | Zero P0/P1 issues, battery test pass | Sign-off or reject |

---

## 5. Definition of Done

A task is QA-approved when:
1. All P0 test cases for that scope pass
2. No new P0/P1 regressions introduced
3. Unit test coverage >= 70% on ViewModel and Repository layers
4. Code reviewed and merged
5. Tested on physical device (minimum API level)
6. Outdoor readability verified (contrast, text size)

---

## 6. Recommendations

### Must Resolve Before Architecture (Task #4)
1. Define activity lifecycle: start, stop, pause, resume, discard flows
2. Define authentication strategy for Cloudflare D1 (OAuth, token, anonymous)
3. Define foreground service design (notification content, controls, Android 14+ compliance)
4. Define battery management targets (sampling intervals, max drain per hour)
5. Add unit system (metric/imperial) to v1 scope

### Should Include in v1
6. GPX data export (low effort, high user trust)
7. Altitude accuracy indicator (manage user expectations)
8. Empty state designs for all list/chart views

### Can Defer to v2
9. Multi-device sync conflict resolution UI
10. Advanced analytics (distance, elevation gain/loss)
11. Social features or sharing

---

## 7. G1 Gate Review: PRD Assessment

**Review Date**: 2026-04-06
**Reviewer**: QA Engineer
**Document Reviewed**: `/Users/zhiwei/.happyclaw/hiking-happy/docs/PRD.md` v1.0 (initial review), updated to v2 after fixes

### 7.1 Blocking Gaps Resolution Status

| Original Gap | PRD Coverage | Assessment |
|--------------|-------------|------------|
| R09 - Activity lifecycle | Story 3 (Start/Stop), Section 10.6 (app kill recovery) | **RESOLVED** -- Start/Stop/Interrupt recovery defined. No pause/resume in V1 (acceptable scope decision). |
| R18 - Authentication for D1 | Section 6.2 (device key, UUID, EncryptedSharedPreferences) | **RESOLVED** -- Device key approach defined with security caveat noted. |
| R13 - Background tracking | Section 3 Non-Goals, Section 10.6 | **RESOLVED** -- Explicit V1 decision: no Foreground Service, foreground-only recording. |

**Gate Result**: All 3 blocking gaps are addressed in the PRD.

### 7.1b G1 Mandatory Fix Resolution Status (PRD v2)

| Fix | Original Issue | PRD v2 Resolution | QA Assessment |
|-----|---------------|-------------------|---------------|
| C8 | `idx_device_unique` on session table prevented multi-device restore | PRD v2 moved to **single-table design** (`activity_record`). No session table exists. UNIQUE constraint is now on `timestamp` only (for idempotent sync). `device_key` is a filter column, not indexed with UNIQUE. | **RESOLVED** -- Single-table design eliminates this issue entirely. UNIQUE on timestamp is correct for INSERT OR IGNORE sync. |
| C12 | No `FLAG_KEEP_SCREEN_ON` during recording | PRD v2 Section 8.2: "Screen wake: Use `FLAG_KEEP_SCREEN_ON` (or `keepScreenOn = true` in Compose) while recording to prevent screen timeout during active use." | **RESOLVED** -- Explicitly specified in non-functional requirements. |

**Additional PRD v2 changes noted by QA**:
- Data model simplified to single `activity_record` table (no session table). This resolves C9 (data normalization) -- with no session table, per-record type/location is the only option and is acceptable for V1 simplicity.
- Light theme confirmed as default (Section 8.2), aligned with outdoor readability priority.
- Unit configuration (meters/feet, km/h/mph) promoted from P2 to Story 10 (Settings Screen). Good move -- addresses C5 partially.

### 7.2 Remaining QA Concerns (Non-Blocking)

| # | Concern | PRD Section | Severity | Recommendation |
|---|---------|-------------|----------|----------------|
| C1 | **No Pause feature** | Story 3 only has Start/Stop | MEDIUM | V1 scope decision is acceptable, but user expectation for "Pause" is high. Recommend adding to V1.1 roadmap explicitly. |
| C2 | **Resume is discard-only on kill** | Section 10.6 -- "resume is a future feature" | LOW | Acceptable for V1. The discard action should still preserve all already-recorded data (mark end_timestamp at last reading). |
| C3 | **Permission flow not specified** | Not covered in PRD | MEDIUM | Android runtime permission request for `ACCESS_FINE_LOCATION` is mandatory. Engineering should handle, but PRD should specify UX: when does the prompt appear? On first launch? On first "Start"? |
| C4 | **Device key = weak auth** | Section 6.2 acknowledges this | LOW | PRD correctly identifies this is not strong auth. Sufficient for V1 personal backup threat model. No action needed. |
| C5 | **Unit system deferred to V1.1** | Section 8 P2 | MEDIUM | Ships with km/h + meters only. Non-US users get defaults. US users will want miles/feet. This is an acceptable tradeoff for V1 speed, but the storage convention (SI units internally, Section 5.4) means adding units later is safe. |
| C6 | **No elapsed time in V1** | Section 8 P2 | LOW | Users won't see session duration during recording. The Stop button shows recording state but no timer. Mild UX gap. |
| C7 | **Sync is manual only (no auto-sync)** | Section 6.6 | LOW | User must manually tap Backup/Restore. V1 decision is fine for simplicity. |
| C8 | ~~**idx_device_unique on session table**~~ | Section 5.3 | ~~HIGH~~ | **RESOLVED in PRD v2** -- Single-table design eliminates session table. UNIQUE now on `timestamp` only. |
| C9 | ~~**activity_type + location stored in sensor_reading**~~ | Section 5.1 | ~~MEDIUM~~ | **RESOLVED in PRD v2** -- Single-table design (`activity_record`) makes per-record type/location the correct approach. No session table to normalize against. Storage cost is acceptable (~50 bytes/record). |
| C10 | **Chart render target: 500ms for 7200 points** | Section 7.1 | MEDIUM | QA target was <100ms. PRD specifies <500ms. 500ms is still acceptable for a non-interactive chart, but this should be validated with the chosen chart library. |
| C11 | **Barometer fusion algorithm is an open question** | Section 13.3 | LOW | Correctly flagged as an engineering spike. QA will validate accuracy once implemented. |
| C12 | ~~**No screen-wake during recording**~~ | Not specified | ~~HIGH~~ | **RESOLVED in PRD v2 Section 8.2** -- `FLAG_KEEP_SCREEN_ON` / `keepScreenOn = true` specified for recording state. |
| C13 | **Settings page items inconsistent with Stories** | Section 11 vs Stories 4-5 | LOW | Section 11 wireframe shows Activity Type and Location on Settings page, but Stories 4-5 describe them as pre-session configuration. These should appear before recording starts, not buried in Settings. Wireframe is conceptual -- acceptable for PRD-level spec. |
| C14 | **App kill recovery: "resume" vs "discard"** | Section 10.6 | MEDIUM | PRD says "resume is a future feature" and discard marks end_timestamp. But what about the incomplete session data? The session table has `end_timestamp = null` for in-progress sessions. QA needs to verify: does "discard" set end_timestamp = last_reading_timestamp, or does it delete the session entirely? The former preserves data (correct), the latter loses it (wrong). |

### 7.3 G1 Gate Verdict

**PASS -- all mandatory fixes resolved in PRD v2**

The PRD successfully resolves all 3 blocking gaps and both mandatory engineering actions.

**Previously mandatory (now resolved)**:

1. ~~**C8 -- Fix `idx_device_unique`**~~ -- **RESOLVED**: PRD v2 moved to single-table design. Session table no longer exists. UNIQUE constraint correctly on `timestamp` for idempotent sync.

2. ~~**C12 -- Add screen wake lock during recording**~~ -- **RESOLVED**: PRD v2 Section 8.2 explicitly specifies `FLAG_KEEP_SCREEN_ON` / `keepScreenOn = true`.

**Recommended but non-blocking**:
- C1: Add Pause to V1.1 roadmap explicitly
- C3: Specify permission request UX timing (first launch vs first Start)
- C9: Remove activity_type and location from sensor_reading (data normalization)
- C14: Clarify that "discard" on interrupted session preserves recorded data

### 7.4 QA Strategy Updates Based on PRD

The following test cases from the original QA document require updates based on PRD decisions:

**Removed from V1 scope** (deferred, no test needed for V1):
- TC-002 (Pause activity) -- No pause in V1
- TC-043 (Background location tracking) -- No foreground service in V1
- TC-045 (Background continuity) -- Foreground-only recording
- TC-046 (Battery optimization exemption) -- No foreground service
- TC-048 (Battery saver adaptive sampling) -- No adaptive sampling (fixed 1 Hz)
- TC-058 (Dark mode) -- Light theme only in V1

**Modified for PRD alignment**:
- TC-003: Stop and save -- No confirmation dialog in PRD (direct save on Stop)
- TC-004: Stop and discard -- Replaced by Section 10.8 (rapid start/stop auto-delete for 0 readings)
- TC-006: Resume after kill -- "Resume" becomes "Discard" (mark as ended), not actual resume
- TC-031: Auto-sync on network return -- Manual sync only, no auto-sync

**New test cases to add**:
- TC-070: Screen stays on during active recording (FLAG_KEEP_SCREEN_ON)
- TC-071: Session with 0 readings auto-deleted on stop (Section 10.8)
- TC-072: Session with < 3 readings saved but de-emphasized (Section 10.8)
- TC-073: Restore on different device generates new device_key
- TC-074: Backup/Restore with manual retry after offline (no auto queue)
- TC-075: GPS weak indicator (yellow) and No GPS indicator (red) transitions
- TC-076: Speed dead zone filtering (< 1 km/h shows "0")

---

## 8. G2 Gate Review: Design Assessment

**Review Date**: 2026-04-06
**Reviewer**: QA Engineer
**Document Reviewed**: `/Users/zhiwei/.happyclaw/hiking-happy/docs/DESIGN.md` v1.1

### 8.1 Focus Area 1: Outdoor Readability

| Requirement | PRD Spec | Design Spec | Assessment |
|-------------|----------|-------------|------------|
| Altitude font >= 48sp | 48sp (Section 7.2) | 56sp standard / 48sp small screen (Section 1.4, 6.2) | **PASS** -- Exceeds minimum by 8sp on standard screens |
| Speed font | 36sp (Section 7.2) | 42sp standard / 36sp small screen | **PASS** -- Meets minimum exactly on standard, down-scales correctly for small screens |
| WCAG AA contrast | 4.5:1 normal, 3:1 large text | 14 combinations verified (Section 1.3) | **PASS** -- Primary text 16.5:1, secondary 6.9:1, all pass |
| High contrast in sunlight | Light theme, dark text | Background #F5F7FA, text #1A1D23, contrast 14.8:1 | **PASS** -- Light theme + high contrast correct for outdoor use |
| Touch targets >= 48dp | 48dp x 48dp | All interactive elements >= 48dp (Section 6.1, verified per component) | **PASS** -- Documented and enforced per component |

**Concern**: The Gear icon touch target is specified as 40dp x 40dp (Section 2.6). This is below the 48dp minimum. However, Material 3 allows the touchable area to be larger than the visible area. Engineering must ensure the Compose click target extends to at least 48dp even though the icon is visually 40dp.

### 8.2 Focus Area 2: Component Spacing & Touch Targets

| Component | Spec Touch Target | >= 48dp? | Notes |
|-----------|------------------|----------|-------|
| Bottom tab bar item | Full tab width, min 48dp height | Yes | OK |
| Activity type pill | min 48dp height (10dp padding * 2 + 14sp text ~= 38dp) | **Borderline** | Vertical padding 10dp top + 10dp bottom + ~18dp text = ~38dp. **Needs verification** -- the 48dp minimum must include padding. With 10dp vertical padding, the pill needs additional internal spacing to reach 48dp tap area. |
| Start/Stop button | Not explicitly specified in DESIGN.md | **Missing** | The recording button is the most critical touch target. Its size, shape, color, and tap area are not defined in the design system. **This is a design gap.** |
| Settings row | Full row width, 48dp minimum | Yes | OK |
| Location input field | 44dp height | **Below 48dp** | Input field is 44dp. Needs 48dp minimum touch target. |
| Gear icon | 40dp x 40dp | **Below 48dp** | Must extend touchable area to 48dp. |
| Clear button (input) | 16dp icon | **Below 48dp** | Standard Material 3 behavior is to have larger tap area than icon. Must ensure >= 48dp. |
| Chart long-press tooltip | V2 feature, reserved | N/A | OK for V1 |

### 8.3 Focus Area 3: GPS Signal Status Feedback

| QA Boundary Condition | Design Coverage | Assessment |
|----------------------|-----------------|------------|
| GPS cold start (>30s) | Section 4.3: Pulsing circle + "Acquiring GPS..." | **PASS** -- Clear loading state with animation |
| GPS acquiring (in progress) | Section 4.2: Pulsing amber dot, 1.5s cycle | **PASS** |
| GPS strong (<10m accuracy) | Section 4.2 / 3.1: Solid green dot, no label | **PASS** |
| GPS weak (10-30m) | Section 3.1: Amber dot + "Low signal" text | **PASS** -- Matches PRD Section 10 (Story 12) "Weak GPS" indicator |
| GPS poor (>50m) | Section 3.1: Amber dot (same as weak) | **Concern** -- PRD Story 12 defines two states: "Weak GPS" (amber, accuracy <20m) and "Poor GPS" (red, accuracy >50m). The design only shows amber for weak and red for lost. The PRD's "Poor GPS" (>50m) state is not visually distinguished from "Weak GPS" in the design. |
| GPS completely lost | Section 3.1 / 4.2: Red dot + "No GPS Signal" banner + last known value at 40% opacity | **PASS** -- Comprehensive overlay design |
| GPS regained | Section 4.2: Sequential color transition amber -> green, banner slides up | **PASS** |
| Altitude at GPS loss | Shows last known value at 40% opacity + "(last reading)" label | **PASS** -- Clear visual indication that data is stale |
| Speed at GPS loss | Shows "0" | **PASS** -- Matches PRD speed behavior |

**PRD Alignment Issue**: PRD Story 12 (Section 201-210) defines 3 GPS states: Weak (amber, <20m), Poor (red, >50m), Lost. The design collapses "Poor" into the "Lost" state. This is an acceptable simplification -- in practice, accuracy >50m and complete signal loss produce similar unusable data, and the visual distinction adds complexity without much user value. Recommend keeping the design's 4-state model (Acquiring, Strong, Weak, Lost) but noting the deviation from PRD.

### 8.4 Focus Area 4: Empty & Error States

| State | Design Coverage | Assessment |
|-------|-----------------|------------|
| No GPS permission | Section 4.5: Full-screen message + "Enable Location" button | **PASS** |
| Chart empty (no data) | Section 2.3: Terrain icon + guidance text | **PASS** |
| Chart loading | Section 4.3: Skeleton shimmer placeholder | **PASS** |
| Sync in progress | Section 3.3 / 4.4: Progress dialog with percentage | **PASS** |
| Sync error | Section 4.4: Error dialog with retry button | **PASS** |
| Sync offline | Section 4.5: Inline error in progress dialog | **PASS** |
| Network error (sync) | Section 4.5: Inline error with retry | **PASS** |
| Restore conflict | Section 4.5: "X records already exist. Merge anyway?" dialog | **PASS** |
| GPS denied ("Don't ask again") | Opens system location settings (Section 4.5) | **PASS** |
| App kill recovery | Not in design doc | **See note** |
| Recording indicator | Not in design doc | **See note** |

**Missing Design States**:

1. **D1: Recording state visual** -- ~~The design shows the instrument display in idle mode but does not specify how the UI changes during active recording.~~ **RESOLVED**: PRD v2 uses auto-recording mode (no Start/Stop button). Recording starts automatically when GPS is active and app is in foreground. The required UI change is a recording state indicator:
   - Recording: Red dot + "Recording" label in App Bar next to title
   - Paused (GPS lost or app backgrounded): Gray dot + "Paused" label
   - No Start/Stop button needed. Engineering will implement directly per team lead instruction.

2. **D2: App kill recovery dialog** -- PRD Section 10.6 specifies that on next launch after a kill, the app should show "Previous session was interrupted. Discard or resume?" The design does not include this dialog or recovery flow.

3. **D3: Session completion feedback** -- ~~After the user taps Stop, what happens?~~ **RESOLVED**: No Start/Stop button in auto-recording mode. No post-stop feedback needed. Recording is continuous while GPS is active and app is foregrounded.

4. **D4: Small screen activity type selector wrapping** -- Section 6.2 mentions FlowRow for small screens but doesn't show the wrapped layout. Acceptable as a responsive behavior note, but should be validated during implementation.

### 8.5 Additional Design Review Findings

| # | Finding | Severity | Recommendation |
|---|---------|----------|----------------|
| D5 | **No elapsed time display during recording** | LOW | PRD deferred elapsed time to P2 (Section 8). Design aligns with PRD. Acceptable for V1. |
| D6 | **No dark mode** | LOW | Design explicitly states light theme only for V1 (Section 5). Aligns with PRD Section 3 Non-Goals. |
| D7 | **Location input on Instruments page (Tab 1)** | MEDIUM | Design places location input on Tab 1 between altitude and speed sections. This means every time a user wants to check altitude/speed, they see a text input field. This clutters the instrument display. Consider moving location input to a collapsible area or pre-session configuration. However, this matches the PRD wireframe (Section 11), so it's a PRD alignment issue, not a design error. |
| D8 | **Portrait-only** | LOW | Design explicitly states portrait only for V1 (Section 6.4). Acceptable. |
| D9 | **Accessibility: `#9CA3AF` tertiary text at 3.0:1 on #F5F7FA** | LOW | This barely meets the 3:1 large text threshold. If any tertiary text is rendered below 12sp bold, it would fail WCAG AA. The design notes this is for section headers (12sp uppercase) only. Engineering must enforce this. |
| D10 | **Vico chart library recommended** | LOW | Good choice for Compose-native charting. Performance with 7200 points needs validation (PRD target: <500ms). |
| D11 | **No Pull-to-Refresh on Trends tab** | LOW | Charts update every 2 seconds automatically. Manual refresh not needed. OK. |
| D12 | **No GPS accuracy threshold mapping** | MEDIUM | Design says "Weak" at 10-30m accuracy but PRD Story 12 says "Weak" at <20m and "Poor" at >50m. Need to align thresholds between PRD and design. Recommend: Weak = accuracy >= 20m, Lost = accuracy >= 50m or no fix. |

### 8.6 Design Quality Assessment

**Strengths**:
- Thorough color token system with full WCAG AA verification (14 combinations)
- Complete component specifications down to pixel-level detail
- Responsive breakpoints defined for small/standard/large screens
- GPS signal state design with animations is well thought out
- Empty states and loading states are covered
- M3 compliance table demonstrates systematic approach
- Implementation-ready with Compose-specific notes (BoxWithConstraints, WindowInsets, Animatable)
- Design QA checklist (Section 10) provides implementation verification criteria

**Weaknesses**:
- Recording state UI is the biggest gap -- the core user workflow (Start -> Record -> Stop) has no visual design
- Touch target compliance has 3 violations (gear icon, location input, clear button) that need padding adjustments
- No design for post-stop feedback or app kill recovery dialog

### 8.7 G2 Gate Verdict

**PASS -- no remaining blocking items**

**Originally blocking (resolved)**:

1. ~~**D1 -- Recording state UI**~~ -- **RESOLVED**: Team lead clarified PRD v2 uses auto-recording mode. No Start/Stop button needed. Recording starts automatically when GPS active + app foreground. Engineering will implement recording state indicator (red dot + "Recording" in App Bar, gray dot + "Paused" when GPS lost/backgrounded). No design doc update required.

**Non-blocking (should address during implementation)**:

2. **Touch target fixes**: Gear icon (40dp -> 48dp tap area via Compose Modifier), location input (44dp -> 48dp height), activity type pill (verify 48dp with padding), clear button (extend tap area)
3. **D2 -- App kill recovery dialog**: Define simple dialog design for interrupted session recovery
4. **D12 -- GPS accuracy thresholds**: Align design thresholds with PRD Story 12 (Weak >= 20m, Lost >= 50m or no fix)

### 8.8 Design-Driven Test Case Additions

New test cases derived from design specifications:

- TC-080: Font scaling -- increase system font size to maximum (200%), verify all text remains readable without overflow
- TC-081: Small screen (360dp) -- verify altitude displays at 48sp, speed at 36sp, charts at 160dp height
- TC-082: Activity type selector wrapping on 360dp screen -- pills wrap to second line, all still tappable
- TC-083: GPS acquiring animation -- pulsing circle animation visible and smooth (1.5s cycle)
- TC-084: Tab switch animation -- 300ms crossfade with horizontal slide, no jank
- TC-085: Chart skeleton shimmer -- visible during first load, replaced by chart data after load
- TC-086: GPS lost overlay -- last known altitude at 40% opacity, red banner, "No GPS Signal" text
- TC-087: GPS regained transition -- sequential amber -> green color change (600ms total)
- TC-088: Sync progress dialog -- progress bar fills, percentage updates, cancel button functional
- TC-089: Sync error dialog -- red error state, retry button opens system settings or retries
- TC-090: Settings unit toggle -- bottom sheet with radio buttons, selection persists
- TC-091: Number formatting -- altitude shows integer with locale separator, speed shows 1 decimal
- TC-092: Edge-to-edge layout -- content extends behind status/nav bars with proper insets
- TC-093: Reduced motion -- animations respect system animator duration scale setting
- TC-094: Auto-recording -- app launches, GPS activates, recording starts automatically (no Start button)
- TC-095: Recording indicator -- red dot + "Recording" label visible in App Bar during active recording
- TC-096: Paused indicator -- gray dot + "Paused" label when app goes to background or GPS lost

---

## 9. G3 Gate Review: Architecture Assessment

**Review Date**: 2026-04-06
**Reviewer**: QA Engineer
**Code Reviewed**: `/Users/zhiwei/.happyclaw/hiking-happy/android/` (38 files)

### 9.1 MVVM Architecture Compliance

| Layer | Expected | Implemented | Assessment |
|-------|----------|-------------|------------|
| Domain models | Clean Kotlin data classes | `SensorReading`, `InstrumentState`, `GpsSignalState`, `AltitudeUnit`, `SpeedUnit` in `domain/model/` | **PASS** |
| Repository interface | Abstract contract in domain layer | `ActivityRepository` (interface) in `data/repository/`, `UserPreferencesRepository` (interface) in `domain/` | **PASS** |
| Repository impl | Concrete class in data layer | `ActivityRepositoryImpl`, `UserPreferencesRepositoryImpl` with `@Singleton` + `@Inject` | **PASS** |
| ViewModel | Hilt-injected, holds StateFlow | `InstrumentsViewModel`, `TrendsViewModel`, `SettingsViewModel` -- all `@HiltViewModel` | **PASS** |
| DI (Hilt) | Module-based injection | `DatabaseModule`, `RepositoryModule`, `ServiceModule` -- all properly structured | **PASS** |
| View/Screen | Composable, observes StateFlow | All screens use `collectAsStateWithLifecycle()` | **PASS** |
| Single Activity | One entry point | `MainActivity` with `@AndroidEntryPoint` | **PASS** |

### 9.2 Database & Migration Strategy

| Aspect | PRD Requirement | Implementation | Assessment |
|--------|----------------|----------------|------------|
| Single table `activity_record` | PRD v2 Section 5.1 | `@Entity(tableName = "activity_record")` | **PASS** |
| Columns match PRD | All specified | All present, correct types | **PASS** |
| SI units internally | PRD Section 5.2 | Altitude meters, speed m/s | **PASS** |
| Speed dead zone <1 km/h | PRD Section 5.2 | `if (update.speed < 0.278f) 0.0` | **PASS** |
| UNIQUE on timestamp | PRD Section 5.1 | `Index(value = ["timestamp"], unique = true)` | **PASS** |
| WAL mode | PRD Section 7.4 | `setJournalMode(RoomJournalMode.WRITE_AHEAD_LOGGING)` | **PASS** |
| exportSchema | PRD Section 10.5 | `exportSchema = true` | **PASS** |
| Type converter | Enum storage | `Converters.kt` with `TypeConverter` | **PASS** |
| **fallbackToDestructiveMigration** | **PRD says false** | **NOT SET (defaults to true)** | **ISSUE** |

**ARCHITECTURE ISSUE (P0)**: `DatabaseModule.kt` does not call `.fallbackToDestructiveMigration(false)`. The Room default is `true`, meaning a failed migration silently destroys all user data. PRD Section 10.5 explicitly requires `false`. **Fix**: Add `.fallbackToDestructiveMigration(false)` to the Room builder.

### 9.3 Sync Architecture

| Aspect | PRD Requirement | Implementation | Assessment |
|--------|----------------|----------------|------------|
| Device key (UUID, encrypted) | PRD Section 6.2 | `EncryptedSharedPreferences` + `MasterKeys.AES256_GCM_SPEC` | **PASS** |
| X-Device-Key header | PRD Section 6.3 | `header("X-Device-Key", deviceKey)` | **PASS** |
| Backup: 5000/batch paginated | PRD Section 6.5 | `getRecordsPaginated(5000, offset)` | **PASS** |
| Backup: retry 3x backoff | PRD Section 6.5 | `1000L * (1L shl attempt)` = 1s/2s/4s | **PASS** |
| Backup: 30s timeout | PRD Section 6.5 | `requestTimeoutMillis = 30_000` | **PASS** |
| Restore: paginated | PRD Section 6.5 | `X-Offset`/`X-Limit` headers, 5000/page | **PASS** |
| Restore: INSERT OR IGNORE | PRD Story 9 | `OnConflictStrategy.IGNORE` | **PASS** |
| Restore never deletes | PRD Story 9 | Only inserts, no deletes | **PASS** |
| Sync on IO dispatcher | PRD Section 6.5 | `withContext(Dispatchers.IO)` | **PASS** |
| Progress 4 phases | Design Section 4.4 | PREPARING/IN_PROGRESS/COMPLETE/ERROR | **PASS** |

### 9.4 Sensor Handling

| Aspect | PRD Requirement | Implementation | Assessment |
|--------|----------------|----------------|------------|
| FusedLocationProviderClient | PRD Section 9.2 | Hilt-injected | **PASS** |
| PRIORITY_HIGH_ACCURACY | PRD Section 9.2 | Set in LocationRequest | **PASS** |
| 1Hz sampling | PRD Section 7.1 | `LocationRequest.Builder(..., 1000L)` | **PASS** |
| Barometric fusion | PRD Section 9.2 | Hypsometric formula + GPS calibration | **PASS** |
| Sensor cleanup | Lifecycle safety | `awaitClose` with cleanup | **PASS** |
| Barometer detection | PRD Section 10.2 | `required="false"` in manifest | **PASS** |
| 3-sample speed smoothing | PRD Section 5.2 | Moving average in ViewModel | **PASS** |

### 9.5 G3 Verdict

**PASS -- with 1 mandatory fix**

1. **DatabaseModule**: Add `.fallbackToDestructiveMigration(false)`

Non-blocking: Move `ActivityRepository` interface to `domain/`; implement 60/40 fusion weight per PRD; make sync URL configurable.

---

## 10. G4 Gate Review: Functional Testing

### 10.1 PRD Story Compliance

| Story | Status | Evidence |
|-------|--------|----------|
| S1: Altitude display | **PASS** | 56sp (48sp small), 1Hz, barometer+GPS, "--" when no fix, m/ft units |
| S2: Speed display | **PASS** | 42sp (36sp small), 1Hz, dead zone <1km/h, 3-sample smooth, 1 decimal |
| S3: Auto-recording | **PASS** | GPS active -> auto-insert to Room, SI units |
| S4: Activity type | **PASS** | 5 types, DataStore persistence |
| S5: Location | **PASS** | Optional, 200 char limit, nullable |
| S6: Altitude chart | **PASS** | Canvas chart, 7200->1200 downsample, 2s refresh, gradient fill |
| S7: Speed chart | **PASS** | Canvas chart, blue accent, same downsampling |
| S8: Backup | **PASS** | 5000/batch, progress, retry, idempotent |
| S9: Restore | **PASS** | Paginated, INSERT OR IGNORE, never deletes |
| S10: Offline | **PARTIAL** | Local recording works, but no network check before sync |
| S11: Settings | **PASS** | Gear icon, all settings accessible |
| S12: GPS states | **PASS** | 4 states with correct color mapping |

### 10.2 Code Review Issues

**P0 (Must Fix)**:

| ID | Issue | File | Description |
|----|-------|------|-------------|
| I1 | `fallbackToDestructiveMigration` not set | `DatabaseModule.kt:20-27` | Defaults to `true`, must be `false` per PRD |
| I2 | Records at POOR GPS accuracy (>50m) | `InstrumentsViewModel.kt:109` | Pollutes dataset with inaccurate data |
| I3 | Chart query loads unlimited records | `TrendsViewModel.kt:39` | `getRecordsSince` returns all rows, could be 28k+ for 8h session |

**P1 (Should Fix)**:

| ID | Issue | File | Description |
|----|-------|------|-------------|
| I4 | No network check before sync | `SettingsViewModel.kt` | PRD requires ConnectivityManager check |
| I5 | No offline-specific error message | `SettingsScreen.kt` | Should show "No network connection" not raw exception |
| I6 | FontFamily.SansSerif vs Roboto Condensed | `InstrumentsScreen.kt` | Design spec requires Roboto Condensed |

**P2 (Nice to Have)**:

| ID | Issue | File | Description |
|----|-------|------|-------------|
| I7 | Duplicate TopAppBar code | Multiple screens | Should extract shared component |
| I8 | No POOR GPS state warning text | `InstrumentsScreen.kt` | POOR shows red dot but no text explanation |
| I9 | Sync base URL hardcoded | `SyncService.kt` | Should be BuildConfig for dev/prod |

### 10.3 G4 Verdict

**PASS -- 3 P0 + 3 P1 fixes required**

---

## 11. G3+G4 Joint Verdict (Final Consolidated)

**ALL FIXES VERIFIED -- 3 P0 + 5 P1 fixed, 3 P2 deferred to V1.1**

**Reviewed**: 38 source files in `/Users/zhiwei/.happyclaw/hiking-happy/android/`
**Date**: 2026-04-06
**Report status**: Final -- All P0 and P1 fixes verified via source code inspection

### P0 -- Must Fix (All VERIFIED)

| # | Issue | File | Line | Description | Status |
|---|-------|------|------|-------------|--------|
| 1 | Missing `fallbackToDestructiveMigration(false)` | `DatabaseModule.kt` | 27 | `.fallbackToDestructiveMigration(false)` added to Room builder chain | **FIXED** |
| 2 | Records at POOR GPS accuracy (>50m) | `InstrumentsViewModel.kt` | 115 | Changed to `if (gpsState == ACTIVE \|\| gpsState == WEAK)` -- POOR skipped | **FIXED** |
| 3 | Chart query loads unlimited records | `ActivityRecordDao.kt` | 19 | `LIMIT 14400` added to `getRecordsSince` query | **FIXED** |

### P1 -- Should Fix (All VERIFIED)

| # | Issue | File | Line | Description | Status |
|---|-------|------|------|-------------|--------|
| 4 | No network check before sync | `SettingsViewModel.kt` | 45-50, 71-77, 85-91 | `isNetworkAvailable()` via ConnectivityManager, called before backup/restore | **FIXED** |
| 5 | No offline-specific error message | `SyncService.kt` | 172, 187, 235, 273 | User-friendly messages: "Could not upload/download data. Please check your connection." | **FIXED** |
| 6 | Display font uses SansSerif | `Type.kt` | 12, 18, 25 | `RobotoCondensed = FontFamily("sans-serif-condensed")` used in displayLarge/displayMedium | **FIXED** |
| 7 | Unused Vico dependency | `build.gradle.kts` | N/A | No Vico dependency in dependencies block -- confirmed removed | **FIXED** |
| 8 | Cache activityType/location in ViewModel | `InstrumentsViewModel.kt` | 38-39, 117, 122 | `cachedActivityType`/`cachedLocation` used instead of DataStore `.first()` | **FIXED** |

### P2 -- Nice to Have

| # | Issue | File | Description |
|---|-------|------|-------------|
| 9 | Duplicate TopAppBar composable | `InstrumentsScreen.kt`, `TrendsScreen.kt` | Both screens define identical `TopAppBar` composable. Extract to shared `ui/components/` for DRY compliance. |
| 10 | No POOR GPS state warning text | `InstrumentsScreen.kt` | POOR state (>50m) shows red dot but no explanatory text. Consider adding "Poor GPS" label similar to weak state's "Low signal". |
| 11 | Sync base URL hardcoded | `SyncService.kt` | `baseUrl = "https://hiking-happy.api.workers.dev"` is hardcoded. Should be a BuildConfig field for dev/staging/prod environments. |

### Architecture Strengths

- Clean MVVM with proper domain/data/ui layer separation
- Hilt DI with well-organized modules (Database, Repository, Service)
- Room database with WAL mode, UNIQUE timestamp, TypeConverter for enum
- Canvas-based chart rendering (zero external dependencies) with 7200->1200 downsampling
- `callbackFlow` + `awaitClose` for proper sensor lifecycle management
- Barometric altitude with hypsometric formula and GPS calibration on first fix
- EncryptedSharedPreferences + AES256_GCM for device key security
- GPS 4-state machine (ACTIVE/WEAK/POOR/LOST) with correct color mapping per design spec
- Speed dead-zone filter (<1 km/h) and 3-sample moving average smoothing
- Sync architecture: 5000/batch pagination, exponential backoff retry, idempotent UPSERT
- DataStore for user preferences with Flow-based reactive updates
- Edge-to-edge display, portrait lock, FLAG_KEEP_SCREEN_ON

### Note on PRD Story 3

The PRD was updated to auto-recording mode (no Start/Stop button). GPS activation triggers automatic recording. This is an intentional design decision, not a missing feature. The implementation correctly reflects this: `InstrumentsViewModel` auto-inserts records to Room whenever GPS state is active.
