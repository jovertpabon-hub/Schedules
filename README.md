# Schedules — Jetpack Compose Student Timetable App

**Schedules** is a native Android application built with Kotlin, Jetpack Compose, and Material Design 3. Designed specifically for college and university students, it helps track daily class schedules, monitor live countdowns to upcoming lectures, calculate vacant free-time windows, and import official Certificate of Enrollment (COE) PDF timetables.

---

## ☕ Visual Design System

The app features a custom 5-tone coffee palette:
- **Deep Espresso (`#4D2A00`)**: Hero card containers, dark surfaces, and primary icon fills.
- **Dark Mocha (`#623528`)**: Screen titles, primary text, dark card borders, and readable labels.
- **Golden Crema (`#F2A900`)**: Selected day indicators, progress arcs, and status highlights.
- **Warm Roast (`#CC6F00`)**: Primary action elements, FAB container, and active highlights.
- **SteamedMilk (`#F9E6A8`)**: Card backgrounds, secondary informational containers, and subtle fills.
- **Warm Coffee Cream (`#FFFBF2`)**: Screen background surface.

---

## 🚀 Key Features

### 1. Live Countdown Card (`Now Tab`)
- Real-time countdown ticker for active or upcoming classes.
- Circular progress arc for ongoing classes.
- Explicit status badges (`CLASS IN PROGRESS`, `NEXT CLASS`, `SCHEDULE COMPLETED`, `NO CLASSES TODAY`).
- Guarantees internal consistency (timer label displays "REMAINING" for ongoing and "STARTS IN" for upcoming).

### 2. Weekly Timetable Navigation
- Horizontally scrolling weekday selection chips (Monday – Sunday).
- Clearly distinguishes the currently selected day from the actual current day (`todayName`).
- Displays visible class counts per day (`Free` or number of classes).

### 3. Automatic Vacant-Time & Free-Gap Calculation
- Automatically detects free gap windows between classes during the active campus operating window (8:00 AM – 5:00 PM, dynamically expanding if classes start earlier or end later).
- Calculates total occupied class time (merging overlapping class intervals to avoid double-counting) alongside total free time.

### 4. Daily Summary Dashboard
- Displays 3 key daily metrics: **Classes**, **Class Time**, and **Free Time**.
- Includes a scrollable horizontal timeline showing lecture start times and real-time status indicators.

### 5. Full Schedule Management (CRUD)
- Add, edit, and delete class entries with course code, title, start time, end time, and room location.
- Includes single-tap "Undo" recovery for recently deleted classes.

### 6. Certificate of Enrollment (COE) PDF Import
- Extractor and pattern-based parser to import official academic schedule PDFs into the Room database.

### 7. Class Notifications
- Local notifications scheduled via Android `AlarmManager` before class start times.

---

## 🏗️ Architecture & Tech Stack

- **Language**: Kotlin 1.9+
- **UI Framework**: Jetpack Compose, Material Design 3, Custom Canvas
- **Architecture Pattern**: MVVM + Repository Pattern
- **Local Persistence**: Room Database with `LocalTime` Converters
- **Concurrency & State**: Kotlin Coroutines, `StateFlow`, `SharedFlow`
- **Build System**: AGP 8+, Gradle KTS
- **Unit Testing**: JUnit 4 with 14 passing deterministic calculation tests

---

## 🧪 Running Unit Tests

Run unit tests via Gradle:

```bash
./gradlew testDebugUnitTest
```

---

## 📂 Project Structure

```
app/src/main/java/com/example/schedules/
├── MainActivity.kt               # Entry point ComponentActivity
├── SevenCards.kt                 # Main app scaffold and FAB controls
├── SchedulesApplication.kt       # Application class & DI setup
├── data/
│   ├── local/                    # Room Database, DAO, Entity, Converters
│   ├── models/                   # Core data models (CourseClass, ScheduleStatus)
│   ├── notifications/            # AlarmReceiver & Notification Helpers
│   ├── parser/                   # COE PDF Schedule Parser
│   ├── pdf/                      # Android PDF Text Extractor
│   └── repository/               # Room Schedule Repository
└── ui/
    ├── components/
    │   ├── NowTabScreen.kt       # Main schedule screen & calculations
    │   ├── CommonUI.kt           # Empty state views
    │   └── SkeletonLoading.kt    # Shimmer loading skeleton
    ├── dialogs/                  # Add/Edit, Clear All, Settings dialogs
    ├── theme/                    # Color palette, Theme, Typography
    └── viewmodel/                # ScheduleViewModel & Status Calculator
```
