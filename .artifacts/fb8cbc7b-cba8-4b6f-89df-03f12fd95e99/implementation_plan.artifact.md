# Improve Empty Schedule UI

Improve the user experience when there are no classes scheduled for a specific day or when the user has finished all classes for the day. This involves creating a unified empty state component and applying it across different screens.

## Proposed Changes

### [Common Components]

#### [NEW] [EmptyDayView](file:///C:/Users/Admin/OneDrive/Documents/Schedules/app/src/main/java/com/example/schedules/ui/components/CommonUI.kt)
- Create a reusable `EmptyDayView` composable in `CommonUI.kt`.
- This component will feature:
    - An illustrative icon (e.g., `Icons.Default.EventAvailable` or `Icons.Default.Coffee`).
    - A main message (e.g., "All caught up!").
    - A secondary message (e.g., "No classes scheduled for today. Enjoy your break!").
    - Proper centering and styling to match the app's theme.

### [Now Tab Screen]

#### [MODIFY] [NowTabScreen.kt](file:///C:/Users/Admin/OneDrive/Documents/Schedules/app/src/main/java/com/example/schedules/ui/components/NowTabScreen.kt)
- Replace the simple `NoClassesMessage` with the new `EmptyDayView`.
- Update `NowTabHeader` to show a friendlier message when there are no classes.
- Improve the "Remaining Classes" view when empty.

### [Grid View Tab]

#### [MODIFY] [CommonUI.kt](file:///C:/Users/Admin/OneDrive/Documents/Schedules/app/src/main/java/com/example/schedules/ui/components/CommonUI.kt)
- Update `DayMiniCard` to use a more subtle version of the empty state or a better placeholder text.
- Update `DetailedDayCard` to use `EmptyDayView` when a specific day has no classes.

## Verification Plan

### Manual Verification
- Deploy the app and check the "Now" tab when no classes are scheduled (e.g., on a weekend or after all classes end).
- Navigate to a day in the "Grid" tab that has no classes and verify the `DetailedDayCard` empty state.
- Verify that `DayMiniCard` looks good when a day has no classes.
