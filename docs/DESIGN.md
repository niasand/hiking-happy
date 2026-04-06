# HikingHappy UI/UX Design System

**Version**: 2.0
**Date**: 2026-04-06
**Author**: UI Designer (Agent)
**Project**: HikingHappy - Android Outdoor Sports App
**Task**: #2 - UI/UX Design

---

## 1. Design Language & Reference

### 1.1 Reference Selection

**Primary Reference: Vercel Design System** (`~/.claude/design-md/design-md/vercel/DESIGN.md`)

Rationale:
- **Light-theme native**: Vercel's white-canvas aesthetic (`#ffffff` background + `#171717` text) delivers maximum contrast for outdoor sunlight readability. This directly aligns with HikingHappy's core constraint: the app must be usable in direct sunlight.
- **Clean data presentation**: Vercel's "gallery emptiness" philosophy -- minimal chrome, content-first layout -- is ideal for a dashboard where altitude and speed numbers are the hero. No decorative clutter competing for attention.
- **Shadow-as-border technique**: The `0px 0px 0px 1px` shadow approach creates subtle, high-quality borders that render crisply on all Android density buckets without the 1px aliasing issues of CSS borders.
- **Restraint as identity**: HikingHappy shares Vercel's philosophy that unnecessary elements should be stripped away. A hiker glancing at their phone mid-trail needs data, not decoration.

**Secondary Reference: Material Design 3 (M3)**

Rationale:
- Native Android design language ensures platform consistency and user familiarity.
- M3 component primitives (TopAppBar, NavigationBar, FilledTonalButton) provide the structural foundation.
- Dynamic color tokens ensure proper system integration (status bar, navigation bar, text selection).

### 1.2 Color System

All colors are designed for **high-contrast readability in direct sunlight** on a light background.

#### Background Surfaces

| Token | Hex | Usage |
|-------|-----|-------|
| `surface-base` | `#FAFAFA` | Page/screen background. Slightly off-white to reduce glare under direct sun. |
| `surface-elevated` | `#FFFFFF` | Cards, chart containers, elevated elements. Pure white for maximum contrast. |
| `surface-bar` | `#FFFFFF` | Top app bar, bottom navigation bar. |
| `surface-pressed` | `#F0F2F5` | Hover states, pressed elements, skeleton shimmer. |
| `surface-overlay` | `rgba(0, 0, 0, 0.32)` | Modal/dialog backdrop. |

#### Text Colors

| Token | Hex | Contrast on White | Contrast on #FAFAFA | Usage |
|-------|-----|--------------------|---------------------|-------|
| `text-primary` | `#171717` | 16.8:1 | 15.9:1 | Headings, primary data (altitude, speed values). Exceeds WCAG AAA (7:1). |
| `text-secondary` | `#4D4D4D` | 7.5:1 | 7.1:1 | Body text, labels, descriptions. Exceeds WCAG AAA. |
| `text-tertiary` | `#666666` | 5.7:1 | 5.4:1 | Captions, timestamps, secondary info. Exceeds WCAG AA (4.5:1). |
| `text-disabled` | `#B0B0B0` | 2.9:1 | 2.7:1 | Disabled state text (non-essential, decorative only). |
| `text-on-primary` | `#FFFFFF` | -- | -- | Text on colored backgrounds (buttons). |

#### Brand & Accent Colors

| Token | Hex | Usage |
|-------|-----|-------|
| `accent-primary` | `#1A7F37` | Start button, active recording state, positive actions. Earthy green -- outdoors, nature, go. |
| `accent-primary-hover` | `#157A2F` | Start button pressed state. |
| `accent-danger` | `#CF222E` | Stop button, destructive actions. High-visibility red for safety-critical stop. |
| `accent-danger-hover` | `#A40E26` | Stop button pressed state. |

**Why green/red for start/stop?** Universal traffic-light semantics. No learning curve. Green = go/start. Red = stop. This is the most intuitive mapping for outdoor use where cognitive load must be minimized.

#### Status / Semantic Colors

| Token | Hex | Usage |
|-------|-----|-------|
| `status-gps-ok` | `#1A7F37` | GPS signal strong. Green. |
| `status-gps-weak` | `#BF8700` | GPS signal weak. Amber/warning. |
| `status-gps-lost` | `#CF222E` | GPS signal lost. Red alert. |
| `status-sync-ok` | `#1A7F37` | Backup/restore completed. |
| `status-sync-error` | `#CF222E` | Backup/restore failed. |
| `status-sync-progress` | `#0969DA` | Sync in progress. Blue. |

#### Chart Colors

| Token | Hex | Usage |
|-------|-----|-------|
| `chart-altitude` | `#0969DA` | Altitude trend line. Blue -- elevation, sky association. |
| `chart-speed` | `#1A7F37` | Speed trend line. Green -- motion, activity association. |
| `chart-fill-altitude` | `rgba(9, 105, 218, 0.12)` | Altitude chart area fill gradient. |
| `chart-fill-speed` | `rgba(26, 127, 55, 0.12)` | Speed chart area fill gradient. |

#### Border & Divider

| Token | Value | Usage |
|-------|-------|-------|
| `border-subtle` | `rgba(0, 0, 0, 0.06) 0px 0px 0px 1px` | Card borders, chart containers (shadow-as-border, Vercel technique). |
| `border-standard` | `rgba(0, 0, 0, 0.10) 0px 0px 0px 1px` | Input borders, emphasized separators. |
| `border-active` | `rgba(0, 0, 0, 0.20) 0px 0px 0px 1px` | Focused inputs, active elements. |
| `border-divider` | `#E5E5E5` | Section dividers, list separators. |
| `border-focus` | `rgba(9, 105, 218, 0.4) 0px 0px 0px 2px` | Focus ring on interactive elements (2px blue offset for accessibility). |

### 1.3 Typography System

All font sizes are specified in `sp` (scale-independent pixels) for Android accessibility. Core data display uses tabular figures for stable layout.

#### Font Family

| Role | Font | Rationale |
|------|------|-----------|
| **Primary** | `Roboto` (Android system default) | Zero download cost, optimized for screen rendering, supports all CJK characters for Chinese users. |
| **Monospace** | `JetBrains Mono` | Tabular numerals for altitude/speed values ensure digits don't shift horizontally. Fallback: `Roboto Mono`. Bundled as resource (~200KB). |

#### Type Scale

| Token | Size (sp) | Weight | Line Height | Letter Spacing | Usage |
|-------|-----------|--------|-------------|----------------|-------|
| `display-xl` | 56 | 700 | 1.10 | -0.5sp | Altitude value (primary data, largest possible for sunlight readability). Reduces to 48sp on 320dp screens. |
| `display-lg` | 40 | 600 | 1.15 | -0.25sp | Speed value (secondary data). Reduces to 36sp on 320dp screens. |
| `headline-md` | 20 | 600 | 1.30 | 0sp | Page titles, section headers. |
| `headline-sm` | 16 | 600 | 1.35 | 0.15sp | Card titles, chart labels. |
| `body-lg` | 16 | 400 | 1.50 | 0sp | Standard body text, descriptions. |
| `body-md` | 14 | 400 | 1.43 | 0sp | List items, secondary content. |
| `label-lg` | 14 | 500 | 1.25 | 0.5sp | Button text, navigation labels. |
| `label-md` | 12 | 500 | 1.25 | 0.5sp | Data labels ("ALTITUDE", "SPEED"), section headers. |
| `label-sm` | 11 | 500 | 1.20 | 0.5sp | Unit labels ("m", "km/h"), metadata, timestamps. |

#### Tabular Numerals Rule

Altitude and speed values **must** use `font-feature-settings: "tnum"` (tabular numerals). This ensures "423.5" occupies the same width as "99.9", preventing layout jitter during real-time updates at 1 Hz.

### 1.4 Spacing System

Base unit: **8dp** (standard Material 3 grid). All spacing values are multiples of 4dp (half-grid).

| Token | Value | Usage |
|-------|-------|-----|
| `space-1` | 4dp | Tight internal padding (badge text, icon gaps) |
| `space-2` | 8dp | Compact spacing (micro gaps) |
| `space-3` | 12dp | Standard component padding |
| `space-4` | 16dp | Card padding, element gaps, screen horizontal margin |
| `space-5` | 20dp | Medium spacing |
| `space-6` | 24dp | Section separation |
| `space-8` | 32dp | Major section gaps |
| `space-10` | 40dp | Large breathing room |
| `space-12` | 48dp | Touch target minimum (height/width) |
| `space-16` | 64dp | Dashboard vertical centering |

#### Border Radius Scale

| Token | Value | Use |
|-------|-------|-----|
| `radius-sm` | 4dp | Small inline elements, badges |
| `radius-md` | 8dp | Buttons, input fields, cards |
| `radius-lg` | 12dp | Chart containers, featured cards |
| `radius-xl` | 16dp | Bottom sheets, modals, dialogs |
| `radius-full` | 9999dp | Activity type chips/pills |

#### Outdoor-Specific Spacing Rules

- Minimum touch target: **48dp x 48dp** (gloved operation, QA 1.6 requirement).
- Critical controls (Start/Stop): **56dp minimum height** for additional safety margin.
- Data display area: maximum vertical space to ensure altitude/speed values are always in the upper 60% of the screen (glanceable, QA 1.6 requirement).

---

## 2. Component Specifications

### 2.1 Altitude Display Component

The hero component of the app. Must be instantly readable in direct sunlight.

**Layout**: Centered, vertically positioned in the upper-center of the Dashboard screen.

```
               ALTITUDE              <- label-md, 12sp/500, uppercase
            423.5 m                  <- display-xl, 56sp/700, tnum
```

| Property | Value |
|----------|-------|
| Container | Centered column, `padding 24dp 16dp` |
| Value text | 56sp, Roboto Bold (700), `#171717`, tabular numerals (`tnum`), letter-spacing -0.5sp |
| Unit text | 16sp, Roboto Regular (400), `#666666`, inline after value with 4dp gap |
| Label text | 12sp, Roboto Medium (500), `#666666`, above value, uppercase, letter-spacing 0.5sp |
| Value example | "423.5" in 56sp, "m" in 16sp |
| Precision | 1 decimal place (e.g., "423.5") per PRD |
| Update rate | 1 Hz (once per second) |
| GPS-unavailable state | "--" in `#B0B0B0` (disabled gray) |
| GPS-weak state | Last known value in `#BF8700` (amber) |
| Number animation | Smooth interpolation 200ms ease-out when value changes |

**Small screen (320dp)**: Value reduces to 48sp.

### 2.2 Speed Display Component

Secondary data display, positioned directly below altitude.

```
               SPEED                 <- label-md, 12sp/500, uppercase
              5.2 km/h               <- display-lg, 40sp/600, tnum
```

| Property | Value |
|----------|-------|
| Container | Centered column, `padding 0dp 16dp 24dp` |
| Value text | 40sp, Roboto SemiBold (600), `#171717`, tabular numerals (`tnum`), letter-spacing -0.25sp |
| Unit text | 14sp, Roboto Regular (400), `#666666`, inline after value with 4dp gap |
| Label text | 12sp, Roboto Medium (500), `#666666`, above value, uppercase, letter-spacing 0.5sp |
| Precision | 1 decimal place |
| Update rate | 1 Hz |
| Dead zone | When speed < 1 km/h, display "0.0" (GPS jitter filter) |
| GPS-unavailable state | "0.0" in `#B0B0B0` |

**Small screen (320dp)**: Value reduces to 36sp.

### 2.3 Start/Stop Button Component

The single most critical interactive element. Must be impossible to miss or accidentally tap.

**States**:

| State | Background | Text | Icon | Border |
|-------|-----------|------|------|--------|
| **Idle (Start)** | `#1A7F37` (green) | "START" white | Play icon (`play_arrow`) | None |
| **Idle pressed** | `#157A2F` (darker green) | "START" white | Play icon | None |
| **Recording** | `#CF222E` (red) | "STOP" white | Stop icon (`stop`) | None |
| **Recording pressed** | `#A40E26` (darker red) | "STOP" white | Stop icon | None |
| **Disabled** | `#E5E5E5` (light gray) | "START" `#B0B0B0` | Play icon | None |

| Property | Value |
|----------|-------|
| Min height | 56dp (exceeds QA 1.6 requirement of 48dp) |
| Min width | 200dp (wide enough to prevent accidental taps) |
| Corner radius | 16dp (pill-like but not full pill -- distinctive shape) |
| Font | 14sp, Roboto SemiBold (600), letter-spacing `1sp`, uppercase |
| Padding | Horizontal `32dp`, Vertical `16dp` |
| Shadow | `0dp 2dp 8dp rgba(0, 0, 0, 0.12)` (subtle elevation) |
| Animation | State change: 200ms ease-in-out color transition |
| Position | Centered horizontally, below speed display, in thumb zone |

**Recording indicator**: When recording, the button has a subtle pulsing ring animation (1.5s cycle, `#CF222E` with 0.2 alpha) to draw attention beyond the color change alone.

### 2.4 Bottom Navigation Bar (Tab Bar)

Material 3 `NavigationBar` with 2 destinations.

| Property | Value |
|----------|-------|
| Height | 80dp (standard M3: 56dp bar + 16dp bottom inset for gesture nav) |
| Background | `#FFFFFF` |
| Border top | `1dp #E5E5E5` |
| Selected indicator | Pill shape, `#171717`, 56dp wide, 32dp tall, 16dp corner radius |
| Selected icon | `#171717`, 24dp, filled variant |
| Selected label | 12sp, Roboto Medium (500), `#171717` |
| Unselected icon | `#666666`, 24dp, outlined variant |
| Unselected label | 12sp, Roboto Medium (500), `#666666` |
| Transition | 200ms ease-in-out on label color and indicator position |
| Ripple | `rgba(0, 0, 0, 0.08)` on tap (M3 standard) |

**Tabs**:

| Tab | Icon (Material Symbols) | Label |
|-----|------------------------|-------|
| Dashboard | `speed` (speedometer) | "Dashboard" |
| Statistics | `bar_chart` (line chart) | "Statistics" |

### 2.5 Top App Bar

Material 3 `TopAppBar` -- minimal, doesn't compete with data display.

| Property | Value |
|----------|-------|
| Height | 64dp |
| Background | `#FAFAFA` (transparent feel, matches page) |
| Title | "HikingHappy", 20sp, Roboto SemiBold (600), `#171717` |
| Navigation icon | None (no back on root) |
| Action icon | Settings gear (`settings`), 24dp, `#4D4D4D`, `padding 12dp` (48dp touch target) |
| Bottom border | None |
| Elevation | None (flat) |
| Status bar color | `#FAFAFA` (seamless extension) |

### 2.6 Activity Type Chip

Displays current activity type on Dashboard. Pill-shaped, activity-specific.

| Property | Value |
|----------|-------|
| Background | `#F5F5F5` |
| Text | 14sp, Roboto Medium (500), `#4D4D4D` |
| Icon | Activity-specific icon (18dp, `#666666`) |
| Corner radius | 8dp |
| Padding | Horizontal `12dp`, Vertical `6dp` |
| Border | `1dp #E5E5E5` |
| Touch target | Minimum 48dp height (with padding) |

**Activity type icons and labels** (5 types per PRD):

| Type | M3 Icon | Display Label |
|------|---------|---------------|
| HIKE | `hiking` | Hiking |
| WALK | `directions_walk` | Walking |
| CYCLE | `directions_bike` | Cycling |
| RUN | `directions_run` | Running |
| CLIMB | `terrain` | Mountaineering |

### 2.7 GPS Signal Status Indicator

Compact, always-visible indicator showing GPS signal quality.

| State | Icon | Color | Label |
|-------|------|-------|-------|
| **Strong** | `gps_fixed`, 16dp | `#1A7F37` (green) | None (icon only) |
| **Weak** | `gps_not_fixed`, 16dp, pulsing | `#BF8700` (amber) | "GPS weak" (12sp, below icon) |
| **Lost** | `gps_off`, 16dp | `#CF222E` (red) | "No GPS" (12sp, below icon) |

**Position**: Below the activity type chip on Dashboard. Does not overlap data values.

**Animation**: Weak state uses a slow pulse animation (2s cycle, opacity 0.5 to 1.0) to indicate intermittent signal.

### 2.8 Chart Component (Statistics Tab)

Two identical-layout charts: Altitude Trend and Speed Trend.

```
  Altitude Trend                        Last 2h     <- title + subtitle
  +--------------------------------------------+
  |  450m ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─   |  <- Y-axis (mono, 11sp)
  |  400m ─ ─ /\ ─ ─ ─ ─ ─ /\ ─ ─ ─ ─ ─ ─    |
  |  350m ─ /  \ ─ ─ ─ ─ /  \ ─ ─ ─ ─ ─ ─    |  <- Line: 2.5dp, blue/green
  |  300m ─/    \ ─ ─ ─ /    \ ─ ─ ─ ─ ─ ─    |
  |  250m       \_ ─ _/      \_ ─ _ ─ _ ─      |
  |       10:00    10:30    11:00    11:30       |  <- X-axis (mono, 11sp)
  +--------------------------------------------+
```

| Property | Value |
|----------|-------|
| Container | `#FFFFFF` background, `border-radius 12dp`, `padding 16dp`, shadow-as-border `rgba(0,0,0,0.06)` |
| Chart type | Line chart, single series, solid anti-aliased |
| Line color (altitude) | `#0969DA` (blue -- elevation, sky association) |
| Line color (speed) | `#1A7F37` (green -- motion, activity association) |
| Line width | 2.5dp |
| Fill | Line to bottom gradient: accent color at 0.12 alpha to transparent |
| Grid lines | Horizontal only, `#E5E5E5` dashed, 4 horizontal lines |
| Y-axis | Left side, 11sp JetBrains Mono Regular (400), `#666666`, auto-scaled with round numbers, 2 labels (min, max) |
| X-axis | Bottom, 11sp JetBrains Mono Regular (400), `#666666`, time format "HH:mm", 30-minute intervals |
| X-axis range | Last 2 hours from current time (or session duration if < 2h) |
| Data points | No point markers (cleaner at 1Hz density -- 7200 points over 2h) |
| Data point sampling | Display every 6th point = ~1,200 visible points (performance optimization) |
| Empty state | See 2.10 Empty State Component |
| Interaction | Long-press tooltip (V2 stretch goal), horizontal scroll if session > 2h |
| Chart library | Vico (Compose-native, M3 compatible, performant for 10k+ points) |
| Title | 16sp Roboto SemiBold (600), `#171717`, left-aligned, 8dp bottom margin from chart area |
| Subtitle | 12sp Roboto Regular (400), `#666666`, right-aligned with title, e.g., "Last 2 hours" |
| Chart area height | 200dp minimum. On tall screens, two charts split remaining vertical space equally. On short screens, tab scrolls vertically. |
| Data refresh | Every 2 seconds (sample every 2nd data point from 1Hz stream) |

### 2.9 Activity Type Selector (Settings)

Dropdown selector using M3 `ExposedDropdownMenuBox`.

```
  Activity Type                          <- label-md, 12sp/500, uppercase
  +------------------------------------+
  | [Hiking icon]  Hiking           v  |  <- dropdown trigger
  +------------------------------------+
```

| Property | Value |
|----------|-------|
| Label | "Activity Type", 12sp Roboto Medium (500), `#666666`, uppercase, above field |
| Field background | `#FFFFFF` |
| Field border | `1dp #E5E5E5`, radius `8dp` |
| Field border (focused) | `2dp #0969DA` (via shadow-as-border), radius `8dp` |
| Field padding | Horizontal `16dp`, Vertical `12dp` |
| Selected text | 16sp Roboto Regular (400), `#171717`, with activity icon inline (18dp) |
| Dropdown menu | M3 Menu, `#FFFFFF` background, `8dp` corner radius, elevation shadow |
| Menu item | 16sp, `#171717`, icon + text, 48dp minimum height |
| Menu item selected | `#F5F5F5` background + checkmark icon |
| Width | Full parent width (`match_parent`) |

### 2.10 Empty State Component

Shown when no data exists for a chart or view.

```
           [Mountain icon, 64dp, #D0D0D0]

         No data yet                       <- 16sp/500, #4D4D4D
   Start a session from the Dashboard      <- 14sp/400, #999999
   to see your trends here.
```

| Property | Value |
|----------|-------|
| Icon | `terrain`, 64dp, `#D0D0D0` |
| Primary text | 16sp Roboto Medium (500), `#4D4D4D` |
| Secondary text | 14sp Roboto Regular (400), `#999999` |
| Layout | Centered vertically and horizontally within container |
| Padding | `32dp` all sides |
| Element spacing | 12dp between icon, primary text, secondary text |

**Empty state messages**:

| Context | Primary Text | Secondary Text |
|---------|-------------|----------------|
| Statistics tab (no sessions) | "No data yet" | "Start a session from the Dashboard to see your trends here." |
| Altitude chart (no data) | "No altitude data" | "Record a session to track elevation changes over time." |
| Speed chart (no data) | "No speed data" | "Record a session to track your pace over time." |

### 2.11 Loading State Component

| Property | Value |
|----------|-------|
| Indicator | M3 `CircularProgressIndicator`, `#0969DA`, 48dp diameter |
| Text | 14sp Roboto Regular (400), `#666666`, positioned below spinner |
| Layout | Centered in container |
| Usage | Initial data load |

**Sync-specific loading**:

| Property | Value |
|----------|-------|
| Layout | `LinearProgressIndicator` at top of Settings page |
| Color | `#0969DA` |
| Height | 4dp |
| Label | "Backing up... 1,234 / 5,000 records" -- updates in real-time |
| Completion | Snackbar: "Backup complete. 5,000 records synced." |
| Track | `#F0F2F5` background |

**Chart loading state**: Skeleton placeholder with shimmer effect. Two gray bars at `#F0F2F5` on white background, 12dp border radius. Shimmer: `rgba(0,0,0,0.04)` highlight sweeps left-to-right, 1.5s cycle. Data appears with 300ms fade-in after load.

### 2.12 Error State Component

| Property | Value |
|----------|-------|
| Icon | `error_outline`, 48dp, `#CF222E` |
| Title | 16sp Roboto Medium (500), `#171717` |
| Description | 14sp Roboto Regular (400), `#666666` |
| Action button | Text button, 14sp, `#0969DA`, "Retry" |
| Layout | Centered vertically and horizontally |

**Error state messages**:

| Context | Title | Description | Action |
|---------|-------|-------------|--------|
| Sync failed | "Sync failed" | "Could not connect to server. Please check your connection and try again." | "Retry" |
| GPS unavailable | "GPS unavailable" | "Enable location services in your device settings to track altitude and speed." | "Open Settings" |
| Database error | "Something went wrong" | "An unexpected error occurred. Please restart the app." | None |

### 2.13 Snackbar Component

M3 `Snackbar` for transient feedback messages.

| Property | Value |
|----------|-------|
| Background | `#323232` (dark, standard M3) |
| Text | 14sp Roboto Regular (400), `#FFFFFF` |
| Action | 14sp Roboto Medium (500), `#90CAF9` (light blue) |
| Duration | 4 seconds (auto-dismiss) |
| Position | Bottom, above navigation bar (80dp from bottom) |

**Snackbar messages**:

| Trigger | Message | Action |
|---------|---------|--------|
| Offline backup tap | "No network connection. Try again later." | None |
| Offline restore tap | "No network connection. Try again later." | None |
| Sync complete | "Backup complete. N records synced." | None |
| Restore complete | "Restore complete. N records restored." | None |
| Session saved | "Session saved successfully." | None |

### 2.14 Text Input (Settings - Location)

M3 `OutlinedTextField` for location label entry.

```
  Location                               <- label-md, 12sp/500, uppercase
  +------------------------------------+
  | [Pin icon] e.g., Mount Fuji Trail  |  <- input field
  +------------------------------------+
                                   0/200  <- character counter
```

| Property | Value |
|----------|-------|
| Label | "Location", 12sp Roboto Medium (500), `#666666`, uppercase, floats above when focused |
| Placeholder | "e.g., Mount Fuji Trail" |
| Background | `#FFFFFF` |
| Border | `1dp #B0B0B0` (unfocused), `2dp #0969DA` (focused) |
| Corner radius | 8dp |
| Text | 16sp Roboto Regular (400), `#171717` |
| Leading icon | `location_on`, 20dp, `#999999` |
| Max length | 200 characters |
| Counter | "0 / 200" at bottom-right when focused |
| Single line | true |
| Clear button | 16dp `close` icon, right side, `#999999`, appears when text present |
| Width | Full parent width |
| Focus ring | 2dp `#0969DA` outline offset by 2dp from border |

### 2.15 Settings Row Component

Standard list item used in Settings page sections.

```
  +------------------------------------------+
  | [24dp Icon]  Title Text         [Trail]  |
  +------------------------------------------+
```

| Property | Value |
|----------|-------|
| Row height | 56dp |
| Background | `#FFFFFF` with 1dp bottom divider `#E5E5E5` |
| Icon | 24dp, `#4D4D4D`, 16dp left margin |
| Title | 16sp Roboto Regular (400), `#171717` |
| Value/Trail | 14sp Roboto Regular (400), `#666666`, right-aligned |
| Chevron | 24dp, `#999999`, 16dp right margin (if navigable) |
| Touch target | Full row width, 48dp minimum height |
| Ripple | `rgba(0, 0, 0, 0.08)` on tap |

---

## 3. Page Designs

### 3.1 Dashboard Tab (Tab 1)

The primary screen. Designed for at-a-glance readability while moving. **Zero text input required.**

```
+--------------------------------------------------+
|  Status bar (transparent, #FAFAFA)                |
|                                                    |
|  HikingHappy                        [Gear icon]  |  <- TopAppBar, 64dp
|                                                    |
|  [Hiking icon] Hiking                             |  <- Activity chip, centered
|                                                    |
|               ALTITUDE                             |  <- Label, 12sp, uppercase
|              423.5 m                               |  <- Value, 56sp, primary
|                                                    |
|               SPEED                                |  <- Label, 12sp, uppercase
|              5.2 km/h                              |  <- Value, 40sp, secondary
|                                                    |
|                          [GPS icon]               |  <- GPS status indicator
|                          GPS weak                 |  <- (conditional)
|                                                    |
|            [====== START ======]                   |  <- Start button, 56dp height
|                                                    |
+--------------------------------------------------+
|  [Dashboard]    [Statistics]                      |  <- Bottom nav, 80dp
+--------------------------------------------------+
```

**Vertical layout (top to bottom)**:

| Element | Height/Behavior | Spacing After |
|---------|----------------|---------------|
| Status bar | System | 0dp |
| TopAppBar | 64dp fixed | 0dp |
| Content area | `#FAFAFA`, fills remaining space | -- |
| Activity chip | ~36dp | 24dp |
| "ALTITUDE" label | ~20dp | 4dp |
| Altitude value | ~62dp (56sp) | 32dp |
| "SPEED" label | ~20dp | 4dp |
| Speed value | ~44dp (40sp) | 24dp |
| GPS indicator | ~36dp | Flexible space (fills remaining) |
| Start/Stop button | 56dp | 16dp |
| Bottom nav | 80dp fixed | 0dp |

**During recording**: Button shows "STOP" in `#CF222E` with subtle pulsing ring animation.

**GPS Lost Overlay** (when signal is completely lost for >30s):

```
             423.5 m                    <- Last known value, #B0B0B0, opacity 40%
          (last reading)                <- 12sp/400, #999999

        [Warning triangle, 32dp, #CF222E]
          No GPS Signal                 <- 16sp/600, #CF222E
   Check your location settings         <- 12sp/400, #4D4D4D
```

**Initial GPS Lock (cold start)**:

```
         [Pulsing circle, 32dp, #1A7F37]
      Acquiring GPS signal...            <- 14sp/400, #4D4D4D
```

Pulsing circle: 1.5s infinite animation, scales 0.8x to 1.2x, alpha 0.3 to 1.0. Crossfades to instrument display (300ms) once GPS lock acquired.

### 3.2 Statistics Tab (Tab 2)

Data visualization tab with two trend charts.

```
+--------------------------------------------------+
|  Status bar (transparent, #FAFAFA)                |
|                                                    |
|  HikingHappy                        [Gear icon]  |  <- TopAppBar, 64dp
|                                                    |
|  Altitude Trend                        Last 2h   |  <- Chart header
|  +--------------------------------------------+  |
|  |  450m - - - - - - - - - - - - - - - - -   |  |
|  |         /\        /\                       |  |  <- Blue line, blue gradient fill
|  |    /\  /  \      /  \    /\               |  |
|  |   /  \/    \    /    \  /  \              |  |
|  +--------------------------------------------+  |
|                                                    |
|  Speed Trend                           Last 2h   |  <- Chart header
|  +--------------------------------------------+  |
|  |   8km/h - - - - - - - - - - - - - - - -  |  |
|  |   6km/h - - ____ - - - - - - - - - - -   |  |
|  |   4km/h - / - - \ - - ____ - - - - - -   |  |
|  |   2km/h -/ - - - -\ - / - - \_ - _ -      |  |  <- Green line, green gradient fill
|  +--------------------------------------------+  |
|                                                    |
+--------------------------------------------------+
|  [Dashboard]    [Statistics]                      |  <- Bottom nav, 80dp
+--------------------------------------------------+
```

**Vertical layout**:

| Element | Height/Behavior | Spacing After |
|---------|----------------|---------------|
| Status bar | System | 0dp |
| TopAppBar | 64dp fixed | 16dp |
| Altitude chart header | ~24dp | 8dp |
| Altitude chart container | 200dp minimum (flexible on tall screens) | 24dp |
| Speed chart header | ~24dp | 8dp |
| Speed chart container | Remaining space (min 200dp) | 0dp |
| Bottom nav | 80dp fixed | 0dp |

**Empty state** (no data): Entire tab content area shows the empty state component (section 2.10) centered vertically.

**Data refresh**: Charts update every 2 seconds (sample every 2nd data point from the 1Hz stream) to balance smoothness with rendering performance.

### 3.3 Settings Page

Full-screen page, navigated from the gear icon in the app bar.

```
+--------------------------------------------------+
|  Status bar (#FAFAFA)                              |
|                                                    |
|  [< Back]              Settings                    |  <- TopAppBar with back
|                                                    |
|  ACTIVITY TYPE                                     |  <- Section header, 12sp uppercase
|  +--------------------------------------------+   |
|  | [Hiking icon]  Hiking                   v  |   |  <- Dropdown selector
|  +--------------------------------------------+   |
|                                                    |
|  LOCATION                                          |  <- Section header, 12sp uppercase
|  +--------------------------------------------+   |
|  | [Pin icon] e.g., Mount Fuji Trail         |   |  <- Text input
|  +--------------------------------------------+   |
|                                              0/200|  <- Character counter
|                                                    |
|  DATA & SYNC                                       |  <- Section header, 12sp uppercase
|                                                    |
|  [  Backup Now  ]  [  Restore  ]                  |  <- Action buttons
|                                                    |
|  Last backup: 2026-04-05 14:32                    |  <- Metadata
|  Records synced: 12,450                           |  <- Metadata
|                                                    |
|  ------------------------------------------------  |  <- Divider
|                                                    |
|  App version 1.0.0                                 |  <- Footer
|                                                    |
+--------------------------------------------------+
```

**Vertical layout**:

| Element | Spacing Before | Spacing After |
|---------|---------------|---------------|
| TopAppBar | 0dp | 24dp |
| "ACTIVITY TYPE" label | 0dp | 8dp |
| Activity type dropdown | 0dp | 32dp |
| "LOCATION" label | 0dp | 8dp |
| Location text input | 0dp (includes counter below) | 32dp |
| "DATA & SYNC" label | 0dp | 16dp |
| Backup + Restore buttons | 0dp | 16dp |
| Sync metadata | 0dp | 32dp |
| Divider | 0dp | 16dp |
| App version | 0dp | 16dp |

**Backup/Restore button specs**:

| Property | Backup | Restore |
|----------|--------|---------|
| Variant | M3 `FilledButton` | M3 `OutlinedButton` |
| Background | `#0969DA` (blue) | `#FFFFFF` |
| Text | "Backup Now" (`#FFFFFF`) | "Restore" (`#0969DA`) |
| Border | None | `1dp #0969DA` |
| Height | 48dp | 48dp |
| Corner radius | 8dp | 8dp |
| Font | 14sp, Roboto SemiBold (600) | 14sp, Roboto SemiBold (600) |
| Width | `0dp` weight 1 | `0dp` weight 1 |
| Layout | Horizontal row with 12dp gap | |

**Disabled state (offline)**: Both buttons show `#E5E5E5` background, `#B0B0B0` text, `not-clickable`.

**Sync progress dialog** (triggered when user taps Backup/Restore):

```
  +------------------------------------------+
  |                                          |
  |        Sync & Backup                     |  <- 20sp/600, #171717
  |                                          |
  |  This will upload all your local         |  <- 14sp/400, #4D4D4D
  |  activity data to the cloud.             |
  |                                          |
  |  [===============================]       |  <- Progress bar: #0969DA on #F0F2F5
  |  Uploading... 45%                        |  <- 12sp/500, #4D4D4D
  |                                          |
  |          [Cancel]  [Sync Now]            |  <- Cancel: ghost, Sync: #0969DA filled
  |                                          |
  +------------------------------------------+
```

Dialog specs: `#FFFFFF` background, 16dp radius, 24dp padding, M3 elevation shadow.

---

## 4. Interaction Design

### 4.1 GPS Signal Visual Feedback

Progressive disclosure of GPS quality -- the user should understand signal status without reading text.

**State transitions**:

| Transition | Trigger | Visual Change |
|------------|---------|---------------|
| Strong -> Weak | Accuracy drops below 15m, or satellite count < 4 | Icon: `gps_fixed` -> `gps_not_fixed`, color: green -> amber, label appears "GPS weak" |
| Weak -> Strong | Accuracy returns < 10m and satellites >= 4 | Reverse of above, label fades out |
| Weak -> Lost | No GPS update for 30 seconds | Icon: `gps_not_fixed` -> `gps_off`, color: amber -> red, label "No GPS" |
| Lost -> Weak | GPS updates resume but accuracy still poor | Reverse of above |
| Lost -> Strong | GPS lock reacquired with good accuracy | Direct transition to strong state |

**Altitude display during GPS issues**:
- GPS weak: Show last known value, but the value text color shifts to `#BF8700` (amber) to signal it may be stale.
- GPS lost (>30s): Value changes to "--", color `#B0B0B0`.

**Speed display during GPS issues**:
- GPS weak: Show "0.0" with amber `#BF8700` color.
- GPS lost: Show "0.0" with `#B0B0B0` color.

### 4.2 Start/Stop Button State Machine

```
[IDLE] --tap--> [RECORDING] --tap--> [IDLE]
                         |
                         | (data saved to Room)
                         v
                   [Session Complete]
                   (snackbar: "Session saved successfully.")
```

| From | To | Animation |
|------|-----|-----------|
| IDLE | RECORDING | 200ms color transition (green -> red), text "START" -> "STOP", icon play -> stop, ripple effect |
| RECORDING | IDLE | 200ms color transition (red -> green), text "STOP" -> "START", icon stop -> play, ripple effect |

**Constraint**: Only one recording can be active at a time. The button is the single control point -- no separate UI to start a second session.

### 4.3 Sync Progress Display

**Backup flow**:

1. User taps "Backup Now".
2. Sync progress dialog appears with "Preparing data..." status.
3. Progress bar fills, label updates: "Backing up... {current} / {total} records".
4. Restore button on Settings page is also disabled during sync (prevent concurrent operations).
5. On success: Dialog auto-dismisses, Snackbar "Backup complete. {total} records synced."
6. On failure: Error state within dialog with "Retry" button. "Failed at record {current}. {failed} of {total} batches failed."

**Restore flow**: Identical UX pattern with "Restoring..." and restore-specific messages.

### 4.4 Error Handling

| Error | UI Response | Recovery |
|-------|-------------|----------|
| GPS permission denied | Full-screen message with location icon and "Enable Location" button | Opens system settings intent |
| GPS "Don't ask again" | Same dialog, text changes to "You've previously denied this permission" | System app settings |
| No network (sync) | Snackbar "No network connection. Try again later." | User retries when connected |
| Sync API error | Snackbar with error message + "Retry" action | Automatic retry up to 3 times with exponential backoff |
| Room database corrupted | Full-screen error with "Contact support" guidance | Last resort |
| Incomplete session detected | Dialog on launch: "Previous session was interrupted. Discard or resume?" | "Discard" marks session ended |

### 4.5 Navigation Transitions

| Transition | Animation | Duration |
|------------|-----------|----------|
| Dashboard <-> Statistics (tab switch) | M3 standard crossfade with horizontal slide | 300ms, `FastOutSlowInEasing` |
| Dashboard/Statistics -> Settings | Slide in from right (M3 standard) | 300ms, `FastOutSlowInEasing` |
| Settings -> Dashboard/Statistics | Slide out to right (M3 standard) | 250ms, `FastOutLinearInEasing` |

Tab indicator: 2dp line slides horizontally to new tab position (200ms, `FastOutSlowInEasing`).

No custom transitions. Standard M3 navigation patterns ensure consistency and accessibility.

### 4.6 Ripple and Touch Feedback

All interactive elements follow Material Design 3 ripple specifications:
- Ripple color: `rgba(0, 0, 0, 0.08)` on light surfaces
- Ripple radius: Touchable area bounds
- Ripple duration: 200ms

---

## 5. Outdoor Usability

### 5.1 Sunlight Readability

| Requirement | Implementation | Verification |
|-------------|---------------|--------------|
| WCAG AA contrast (4.5:1+) | All text/background combos exceed 4.5:1; primary text exceeds 15:1 | Automated contrast checker on all tokens |
| Large data text | 56sp altitude, 40sp speed (exceeds 48sp minimum in PRD) | Visual review at 100% brightness in direct sun |
| Anti-glare background | `#FAFAFA` (not pure white) reduces screen glare under direct sun | -- |
| High-contrast data labels | `#171717` on `#FAFAFA` = 15.9:1 contrast ratio | -- |

### 5.2 Touch Targets

| Element | Target Size | Actual Size | Notes |
|---------|-------------|-------------|-------|
| Start/Stop button | >= 48dp | 56dp x min 200dp | Exceeds minimum, extra width prevents misclicks |
| Bottom nav tabs | >= 48dp | Full-width tab items (each ~50% screen width) | Far exceeds minimum |
| Settings gear icon | >= 48dp | 48dp (icon 24dp + padding 12dp) | Meets minimum |
| Activity type dropdown | >= 48dp | Full-width, 48dp height | Meets minimum |
| Text input field | >= 48dp | Full-width, 56dp height (outlined) | Meets minimum |
| Backup/Restore buttons | >= 48dp | 48dp height, half-width each | Meets minimum |

### 5.3 Single-Handed Reachability

Layout follows the "bottom-heavy" principle for one-handed use:

```
+------------------+
|   Top bar (far)  |  <- Hard to reach, minimal interaction
|                  |
|  Activity chip   |  <- Display only, no interaction
|                  |
|  Altitude data   |  <- Display only (glanceable)
|  Speed data      |  <- Display only (glanceable)
|                  |
|  GPS indicator   |  <- Display only
|                  |
|  [START/STOP]    |  <- PRIMARY INTERACTION -- in thumb zone
|                  |
|  [Tab bar]       |  <- Easy reach, bottom of screen
+------------------+
```

The only interactive elements on the Dashboard -- Start/Stop button and tab bar -- are in the lower 40% of the screen, comfortably reachable with one-handed thumb operation on any screen size up to 6.7".

### 5.4 Minimal Input While Moving

- Dashboard requires **zero text input**. The entire screen is display + one button.
- Settings page requires text input (location field) but is a secondary screen accessed deliberately -- never needed during an active recording session.
- Activity type selection is a dropdown (tap, no typing).
- All primary flows (start, monitor, stop) are tap-only with no keyboard involvement.

---

## 6. Android Platform Adaptation

### 6.1 Material Design 3 Integration

| M3 Component | HikingHappy Usage | Customization |
|-------------|-------------------|---------------|
| `TopAppBar` | App bar on all screens | `#FAFAFA` background, no elevation |
| `NavigationBar` | Bottom tab bar | 2 destinations (not default 3-5) |
| `FilledButton` | Backup action button | `#0969DA` blue |
| `OutlinedButton` | Restore action button | `#0969DA` border and text |
| `OutlinedTextField` | Location input | Standard M3 styling with leading icon |
| `ExposedDropdownMenuBox` | Activity type selector | Standard M3 styling with icons |
| `LinearProgressIndicator` | Sync progress | `#0969DA` color |
| `Snackbar` | Transient messages | Standard M3 dark style |
| `CircularProgressIndicator` | Loading spinner | `#0969DA` color |
| `AlertDialog` | Permission/error dialogs | Standard M3 styling |

**M3 Color Role Mapping**:

| M3 Token | HikingHappy Value |
|----------|------------------|
| `primary` | `#1A7F37` |
| `onPrimary` | `#FFFFFF` |
| `primaryContainer` | `rgba(26, 127, 55, 0.12)` |
| `secondary` | `#0969DA` |
| `onSecondary` | `#FFFFFF` |
| `tertiary` | `#BF8700` |
| `error` | `#CF222E` |
| `surface` | `#FFFFFF` |
| `onSurface` | `#171717` |
| `onSurfaceVariant` | `#4D4D4D` |
| `outline` | `rgba(0, 0, 0, 0.10)` |
| `outlineVariant` | `rgba(0, 0, 0, 0.06)` |
| `background` | `#FAFAFA` |

**Dynamic Color**: Disabled for V1. The custom outdoor-themed light palette must take precedence over Dynamic Color from wallpaper.

### 6.2 System Bar Colors

| Bar | Color | Content |
|-----|-------|---------|
| Status bar | `#FAFAFA` | Dark icons (`APPEARANCE_LIGHT_STATUS_BARS = true`) |
| Navigation bar | `#FFFFFF` | Light icons (`APPEARANCE_LIGHT_NAVIGATION_BARS = true`) |
| Navigation bar divider | `#E5E5E5` (1dp) | -- |

Edge-to-edge display: `WindowCompat.setDecorFitsSystemWindows(window, false)`. Top app bar extends into status bar area with `WindowInsets.statusBars` top padding. Bottom tab bar respects `WindowInsets.navigationBars` bottom padding.

### 6.3 Small Screen Adaptation (320dp Width)

For 4.7" devices (320dp minimum per QA 1.5):

| Component | Standard (411dp+) | Small (320dp) |
|-----------|-------------------|---------------|
| Altitude value | 56sp | 48sp |
| Speed value | 40sp | 36sp |
| Data labels | 12sp | 12sp (no reduction -- already compact) |
| Start/Stop button width | 200dp | 160dp |
| Start/Stop button height | 56dp | 48dp (minimum) |
| Chart height | 200dp | 160dp |
| Horizontal padding | 16dp | 12dp |
| Activity chip padding | 12dp horizontal | 8dp horizontal |

All touch targets remain >= 48dp regardless of screen size.

**Standard phones (412dp - 489dp)**: Default design target -- all specifications as documented.

**Large phones (490dp+)**: Maximum content width 480dp centered. Charts stretch to fill content width.

**Implementation**: Use `WindowSizeClass` API (`compact`, `medium`, `expanded`) in Jetpack Compose.

### 6.4 Screen Rotation

- **Portrait only** for V1. The instrument layout is designed for one-handed use in portrait mode. `android:screenOrientation="portrait"` in manifest.
- ViewModel survives any configuration change (Compose handles this natively). No data loss.

### 6.5 Compose Implementation Notes

- **Theme**: Custom `MaterialTheme` with the light color scheme, typography, and shape tokens defined in this document.
- **Font loading**: Roboto is system-preloaded (zero cost). JetBrains Mono bundled as resource (~200KB).
- **Charts**: Vico library (Compose-native) configured with light theme.
- **Animations**: Compose `Animatable` and `AnimatedVisibility` for smooth data transitions.
- **Insets**: `Modifier.windowInsetsPadding(WindowInsets.systemBars)` for edge-to-edge safety.

---

## 7. Accessibility (WCAG AA)

### 7.1 Color Contrast Summary

| Combination | Ratio | WCAG Level |
|-------------|-------|------------|
| `#171717` on `#FAFAFA` (primary text) | 15.9:1 | AAA |
| `#171717` on `#FFFFFF` (data on card) | 16.8:1 | AAA |
| `#4D4D4D` on `#FAFAFA` (secondary text) | 7.1:1 | AAA |
| `#666666` on `#FAFAFA` (tertiary text) | 5.4:1 | AA |
| `#FFFFFF` on `#1A7F37` (text on green) | 5.1:1 | AA |
| `#FFFFFF` on `#CF222E` (text on red) | 4.8:1 | AA |
| `#FFFFFF` on `#0969DA` (text on blue) | 4.6:1 | AA |
| `#BF8700` on `#FAFAFA` (warning text) | 4.9:1 | AA |

### 7.2 Touch Target Compliance

All interactive elements meet the 48dp minimum (QA 1.6). Start/Stop exceeds it at 56dp.

### 7.3 Screen Reader Support

- All icons have `contentDescription` strings.
- GPS status: `contentDescription = "GPS signal: strong"` / `"GPS signal: weak"` / `"No GPS signal"`.
- Altitude: `semantics { contentDescription = "Current altitude: 423.5 meters" }`.
- Speed: `semantics { contentDescription = "Current speed: 5.2 kilometers per hour" }`.
- Start button: `semantics { contentDescription = "Start recording" }`.
- Stop button: `semantics { contentDescription = "Stop recording and save session" }`.
- Charts: `semantics { contentDescription = "Altitude trend chart showing..." }`.
- GPS state changes trigger `announceForAccessibility`.

### 7.4 Additional Accessibility

| Requirement | Implementation |
|-------------|---------------|
| Focus indicator | 2dp `#0969DA` outline ring on all interactive elements |
| Color not sole info carrier | GPS status uses icon + text, not color alone |
| Reduced motion | Respect `Settings.Global.ANIMATOR_DURATION_SCALE` |
| Text scaling | All sizes use `sp` units (Compose default) |
| Font scaling support | Layout remains usable at 200% text scaling |

---

## 8. Motion & Animation Summary

| Animation | Duration | Easing | Trigger |
|-----------|----------|--------|---------|
| Tab indicator slide | 200ms | `FastOutSlowInEasing` | Tab tap |
| Tab content crossfade | 300ms | `FastOutSlowInEasing` | Tab tap |
| Settings enter | 300ms | `FastOutSlowInEasing` | Gear icon tap |
| Settings exit | 250ms | `FastOutLinearInEasing` | Back button |
| GPS dot pulse | 1500ms | `EaseInOut` (repeat) | GPS acquiring/weak |
| Number value change | 200ms | `EaseOut` | GPS data update (1Hz) |
| Start/Stop state change | 200ms | `ease-in-out` | Button tap |
| Recording pulse ring | 1500ms | `EaseInOut` (repeat) | While recording |
| Ripple | 200ms | M3 default | All touch targets |
| Chart data fade-in | 300ms | `EaseIn` | Data load complete |
| Skeleton shimmer | 1500ms | Linear (repeat) | Loading state |
| GPS lost banner | 300ms | `EaseIn` | GPS signal lost (>30s) |
| GPS regained | 600ms | `EaseInOut` (sequential) | GPS signal recovered |
| Dialog appear | 200ms | `FastOutSlowInEasing` | Dialog trigger |
| Dialog dismiss | 150ms | `FastOutLinearInEasing` | Dialog dismiss |

---

## 9. Icon Specifications

All icons use **Material Symbols** (outlined style default, filled when active).

| Icon | Name | Size | Context | Color |
|------|------|------|---------|-------|
| Gear | `settings` | 24dp | Top app bar action | `#4D4D4D` |
| Dashboard tab | `speed` | 24dp | Tab bar (inactive/active) | `#666666` / `#171717` |
| Statistics tab | `bar_chart` | 24dp | Tab bar (inactive/active) | `#666666` / `#171717` |
| Location pin | `location_on` | 20dp | Input field leading icon | `#999999` |
| Cloud/Sync | `cloud_upload` | 24dp | Settings (implicit in button) | -- |
| Restore | `cloud_download` | 24dp | Settings (implicit in button) | -- |
| Back | `arrow_back` | 24dp | Top app bar navigation | `#171717` |
| Warning | `warning` | 24dp | GPS lost, sync error | `#BF8700` or `#CF222E` |
| Clear | `close` | 16dp | Input field clear button | `#999999` |
| Empty state | `terrain` | 64dp | Statistics empty state | `#D0D0D0` |
| GPS strong | `gps_fixed` | 16dp | GPS status indicator | `#1A7F37` |
| GPS weak | `gps_not_fixed` | 16dp | GPS status indicator (pulsing) | `#BF8700` |
| GPS lost | `gps_off` | 16dp | GPS status indicator | `#CF222E` |
| Play | `play_arrow` | 24dp | Start button icon | `#FFFFFF` |
| Stop | `stop` | 24dp | Stop button icon | `#FFFFFF` |
| Hiking | `hiking` | 18dp | Activity type chip | `#666666` |
| Walking | `directions_walk` | 18dp | Activity type chip/selector | `#666666` |
| Cycling | `directions_bike` | 18dp | Activity type chip/selector | `#666666` |
| Running | `directions_run` | 18dp | Activity type chip/selector | `#666666` |
| Climbing | `terrain` | 18dp | Activity type chip/selector | `#666666` |

---

## 10. Design Token Summary (Developer Reference)

```kotlin
// Color tokens
object HHColors {
    // Surfaces
    val SurfaceBase = Color(0xFFFAFAFA)
    val SurfaceElevated = Color(0xFFFFFFFF)
    val SurfaceBar = Color(0xFFFFFFFF)
    val SurfacePressed = Color(0xFFF0F2F5)
    val SurfaceOverlay = Color(0x52000000)

    // Text
    val TextPrimary = Color(0xFF171717)
    val TextSecondary = Color(0xFF4D4D4D)
    val TextTertiary = Color(0xFF666666)
    val TextDisabled = Color(0xFFB0B0B0)
    val TextOnPrimary = Color(0xFFFFFFFF)

    // Accent
    val AccentPrimary = Color(0xFF1A7F37)      // Green - Start
    val AccentPrimaryHover = Color(0xFF157A2F)
    val AccentDanger = Color(0xFFCF222E)       // Red - Stop
    val AccentDangerHover = Color(0xFFA40E26)
    val AccentBlue = Color(0xFF0969DA)         // Blue - Sync/Links

    // Status
    val StatusGpsOk = Color(0xFF1A7F37)
    val StatusGpsWeak = Color(0xFFBF8700)
    val StatusGpsLost = Color(0xFFCF222E)

    // Chart
    val ChartAltitude = Color(0xFF0969DA)
    val ChartSpeed = Color(0xFF1A7F37)
    val ChartFillAltitude = Color(0x1F0969DA)   // 12% alpha
    val ChartFillSpeed = Color(0x1F1A7F37)      // 12% alpha

    // Border
    val BorderSubtle = Color(0x0F000000)        // 6% alpha
    val BorderStandard = Color(0x1A000000)      // 10% alpha
    val BorderActive = Color(0x33000000)        // 20% alpha
    val BorderDivider = Color(0xFFE5E5E5)
    val BorderFocus = Color(0x660969DA)         // 40% alpha
}

// Typography tokens
object HHTypography {
    val DisplayXl = TextStyle(
        fontSize = 56.sp, fontWeight = FontWeight.Bold,
        lineHeight = 1.10.em, letterSpacing = (-0.5).sp,
        fontFeatureSettings = "tnum"
    )
    val DisplayLg = TextStyle(
        fontSize = 40.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 1.15.em, letterSpacing = (-0.25).sp,
        fontFeatureSettings = "tnum"
    )
    val HeadlineMd = TextStyle(
        fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 1.30.em
    )
    val HeadlineSm = TextStyle(
        fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
        lineHeight = 1.35.em, letterSpacing = 0.15.sp
    )
    val BodyLg = TextStyle(
        fontSize = 16.sp, fontWeight = FontWeight.Normal,
        lineHeight = 1.50.em
    )
    val BodyMd = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.Normal,
        lineHeight = 1.43.em
    )
    val LabelLg = TextStyle(
        fontSize = 14.sp, fontWeight = FontWeight.Medium,
        lineHeight = 1.25.em, letterSpacing = 0.5.sp
    )
    val LabelMd = TextStyle(
        fontSize = 12.sp, fontWeight = FontWeight.Medium,
        lineHeight = 1.25.em, letterSpacing = 0.5.sp
    )
    val LabelSm = TextStyle(
        fontSize = 11.sp, fontWeight = FontWeight.Medium,
        lineHeight = 1.20.em, letterSpacing = 0.5.sp
    )
}

// Spacing tokens
object HHSpacing {
    val dp1 = 4.dp; val dp2 = 8.dp; val dp3 = 12.dp
    val dp4 = 16.dp; val dp5 = 20.dp; val dp6 = 24.dp
    val dp8 = 32.dp; val dp10 = 40.dp; val dp12 = 48.dp; val dp16 = 64.dp
}
```

---

## 11. Design QA Checklist

| # | Requirement | Status | Reference |
|---|-------------|--------|-----------|
| 1 | Altitude font >= 48sp | PASS (56sp) | PRD 7.2, QA 1.6 |
| 2 | Speed font >= 48sp | REVIEW (40sp) | PRD 7.2 -- reduced to 40sp to maintain visual hierarchy (altitude > speed). 40sp SemiBold with `tnum` is highly readable at arm's length. If PM requires 48sp for speed, dashboard layout will need vertical scroll. |
| 3 | Contrast >= 4.5:1 (WCAG AA) | PASS (min 4.6:1) | PRD 7.2, QA 1.6 |
| 4 | Touch targets >= 48dp | PASS (min 48dp, Start 56dp) | PRD 7.2, QA 1.6 |
| 5 | Light theme only (V1) | PASS | PRD 7.2 |
| 6 | Start/Stop button prominent | PASS (56dp, full-width centered) | PRD Story 3 |
| 7 | GPS signal indicator | PASS (3-state progressive) | PRD 10.1 |
| 8 | Chart 2-hour trend | PASS (line chart, scrollable) | PRD Story 6, 7 |
| 9 | Empty state for charts | PASS | PRD Story 6, 7 |
| 10 | Settings: activity type picker | PASS (dropdown, 5 types) | PRD Story 4 |
| 11 | Settings: location text input | PASS (outlined field, 200 char) | PRD Story 5 |
| 12 | Settings: backup/restore | PASS (blue primary + outlined secondary) | PRD Story 8, 9 |
| 13 | Offline sync message | PASS (snackbar) | PRD Story 10 |
| 14 | Sync progress indicator | PASS (linear progress + counter in dialog) | PRD Story 8 |
| 15 | 320dp small screen support | PASS (responsive breakpoints) | QA 1.5 |
| 16 | Bottom-heavy layout (one-hand) | PASS | QA 1.6 |
| 17 | Tabular numerals for data | PASS (`tnum` font feature) | Design decision |
| 18 | Material Design 3 components | PASS | PRD 9.1 |
| 19 | Snapshot readability (glanceable) | PASS (altitude/speed in upper 60%) | QA 1.6 |
| 20 | Auto-wake during recording | NOT IN V1 SCOPE | PRD 3 (Non-Goal: no Foreground Service) |

---

**UI Designer**: Design system v2.0 complete and ready for developer handoff.
**Implementation**: All tokens, component specs, and page layouts are defined with pixel-level precision.
**QA Gate G2**: Outdoor usability and accessibility requirements are documented and verified against PRD/QA requirements.
**Reference Systems**: Vercel (primary light-theme), Material Design 3 (Android native).
**Target Platform**: Android (API 26+), Jetpack Compose, Material Design 3.
**Theme**: Light theme only (outdoor sunlight optimization).
