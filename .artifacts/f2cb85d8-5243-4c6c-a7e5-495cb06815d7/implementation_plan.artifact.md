# Fix NullPointerException in RecyclerView$LayoutManager

The application crashes with a `NullPointerException` when navigating to the Music Statistics screen. This is due to `fragment_music_display.xml` (and its `w600dp` variant) using `androidx.recyclerview.widget.RecyclerView` as the root layout while containing static children (`TextView`, `ScrollView`) instead of using an adapter. `RecyclerView` expects all children to be managed by a `ViewHolder`, and fails when attempting to layout static children defined in XML.

## Proposed Changes

### Android Resources

#### [MODIFY] [fragment_music_display.xml](file:///C:/DevelopmentProjects/MediaLibrary/app/src/main/res/layout/fragment_music_display.xml)
- Replace `androidx.recyclerview.widget.RecyclerView` with `androidx.constraintlayout.widget.ConstraintLayout`.
- Remove `RecyclerView`-specific attributes like `app:layoutManager`.

#### [MODIFY] [fragment_music_display.xml (w600dp)](file:///C:/DevelopmentProjects/MediaLibrary/app/src/main/res/layout-w600dp/fragment_music_display.xml)
- Replace `androidx.recyclerview.widget.RecyclerView` with `androidx.constraintlayout.widget.ConstraintLayout`.
- Remove `RecyclerView`-specific attributes like `app:layoutManager` and `app:spanCount`.

## Verification Plan

### Manual Verification
- Deploy the app to a device or emulator.
- Navigate to the "See All Cds" section from the main screen.
- Verify that the Music Statistics screen displays correctly without crashing.
- Test on both standard and wide-screen (w600dp) configurations.
