# Implementation Plan: Trellis Anti-Burnout System, Sanctuary Garden & Health Connect

This plan outlines the architecture and changes required to implement Anti-Burnout mechanics, Negative Habit tracking, Health Connect auto-sync, Variable Intensity metrics, Flexible Weekly Frequencies, and the Trellis Sanctuary Garden visual evolution.

---

## 1. Core Feature Highlights

1. **Anti-Burnout & Momentum Forgiveness Mechanics**:
   - **Streak Shields**: Earned via consistency and Clean Days; automatically shields a missed day without breaking your streak.
   - **Momentum Score**: Soft reduction (-1 day of momentum) instead of resetting a 100-day streak to 0.
   - **Bounce-Back Rate**: Tracks recovery rate after a missed day and grants bonus XP for returning promptly.

2. **Trellis Sanctuary Garden (Visual Evolution)**:
   - Dynamic interactive visual world that grows from a seed sprout $\rightarrow$ sapling $\rightarrow$ blossoming tree $\rightarrow$ ancient sanctuary grove as user XP increases.
   - Interactive garden card on the Overview dashboard showing live flora growth, garden health score, and unlocked flora tiers.

3. **Negative Habit Tracking (Break Bad Habits)**:
   - Dedicated "Break Bad Habit" mode (e.g. doomscrolling, smoking, excessive sugar).
   - Tracks **Clean Days**, urge logs, and rewards abstaining with bonus XP and Streak Shields.

4. **Health Connect Auto-Tracking (Samsung S24 Ultra Compatible)**:
   - Integration with Jetpack `androidx.health.connect:connect-client` for auto-logging **Daily Step Count** and **Active Distance**.
   - Auto-checks tied habits when Health Connect goals are met.

5. **Variable Metrics & Intensity Tracking**:
   - Supports logging custom units (Steps, Kilometers, Minutes, Pages, Reps) or rating effort intensity on a 1–10 scale.

6. **Flexible Frequencies**:
   - Supports custom weekly target frequencies (e.g. "3 times a week", "5 times a week") in addition to standard daily compliance.

---

## 2. Technical Architecture & File Changes

### Data Layer (`com.example.data`)
- **`Entities.kt`**:
  - Update `HabitEntity` with fields: `habitType` (`POSITIVE` / `NEGATIVE`), `targetFrequency` (`DAILY` / `WEEKLY_X_TIMES`), `weeklyGoalCount` (Int), `unitType` (`COMPLETION`, `STEPS`, `KM`, `MINUTES`, `PAGES`, `REPS`, `INTENSITY_SCALE`), `targetUnitValue` (Double), `momentumScore` (Int), `streakShields` (Int).
  - Update `HabitLogEntity` with: `numericValue` (Double), `intensityRating` (Int 1-10), `isShieldUsed` (Boolean), `isNegativeCleanDay` (Boolean).
  - Add `GardenStateEntity`: Garden tier level, plant stage, health score, unlocked items.
- **`Daos.kt`**:
  - Queries for fetching negative habits, calculating weekly target completions, and updating garden state.
- **`HabitRepository.kt`**:
  - Add momentum calculation engine, streak shield consumption logic, negative habit clean day logging, and Health Connect data bridge.

### Health Connect Integration (`com.example.data.health`)
- **`HealthConnectManager.kt`**:
  - Manages permissions (`READ_STEPS`, `READ_DISTANCE`) and queries Samsung Health / Health Connect for today's step count and active distance.

### UI Components (`com.example.ui.components`)
- **`SanctuaryGardenCard.kt`**:
  - Visual garden view rendering animated plant growth stages, sanctuary trees, and garden health metrics.
- **`HabitLoggingModal.kt`**:
  - Interactive popup modal allowing users to log specific values (e.g., 8,500 steps, 15 pages) or slide an intensity scale (1–10) when checking off habits.
- **`HealthConnectBar.kt`**:
  - Status pill displaying real-time Health Connect sync status (Steps & Distance) with a manual "Sync Now" trigger.

### Screens & ViewModels (`com.example.ui.screens`, `HabitFlowViewModel.kt`)
- **`DashboardScreen.kt`**: Feature the **Sanctuary Garden Card**, Momentum Score, and Health Connect status pill.
- **`HabitGridScreen.kt` / `MobileHabitGrid.kt`**: Support flexible frequency indicators, Clean Day badges for negative habits, unit logging popups, and Streak Shield indicators.
- **`AddHabitDialog.kt`**: Expanded dialog with tabbed toggle between "Build Good Habit" vs "Break Bad Habit", unit selection, weekly frequency dropdown, and Health Connect auto-sync toggle.

---

## 3. Verification Plan

1. **Build Verification**:
   - Execute `compile_applet` to confirm zero syntax or build regressions.
2. **Feature Flow Verification**:
   - Verify creation of Positive vs. Negative habits with custom unit types and weekly frequencies.
   - Verify Anti-Burnout Momentum calculation and Streak Shield retention logic.
   - Verify visual evolution updates in the Trellis Sanctuary Garden card as user completes habits.
   - Verify Health Connect permission flow and step/distance auto-sync bridge.
