name: "Debug Mode"
description: |
  ## Purpose
  Add a debug mode toggle on the home screen. When enabled, the Bin Locator
  screen exposes tools to inspect raw OCR output and captured crops, and
  prevents sending records to the server.

  ## Core Principles
  1. **Context is King**: reference Android docs and existing activity patterns.
  2. **Validation Loops**: Gradle lint plus unit/instrumentation tests.
  3. **Information Dense**: follow Kotlin utility patterns in the repo.
  4. **Progressive Success**: implement toggle, debug UI, then integrate.
  5. **Global rules**: follow CODEX.md guidelines.

---

## Goal
Enable a user controlled "debug" mode. The main activity shows a checkbox to
launch the Bin Locator with debugging enabled. In this mode:
- Sending a record is disabled.
- A button shows the most recent unparsed OCR lines with bounding box heights.
- Another button toggles visibility of the bounding box overlay, replacing it
  with the last captured crop bitmap.

## Why
- **Business value**: allows easier troubleshooting of OCR issues on devices.
- **Integration**: builds on MainActivity and BinLocatorActivity flows.
- **Problem solved**: users currently cannot inspect raw OCR data or capture
  regions.

## What
- Update layouts and activities to support the debug toggle and UI.
- Store the debug flag via Intent extras.
- Keep patterns consistent with existing button callbacks and Snackbar usage.

### Success Criteria
- [ ] Debug checkbox on home screen launches Bin Locator in debug mode.
- [ ] In debug mode Send Record is hidden and disabled.
- [ ] "Show OCR" button displays raw text and bounding box sizes.
- [ ] "Show Crop" button toggles bounding box overlay and crop preview.
- [ ] Gradle lint and tests pass.

## All Needed Context

### Documentation & References
```yaml
- url: https://developer.android.com/guide/topics/ui/controls/checkbox
  why: Implementing CheckBox UI element.
- url: https://developer.android.com/guide/topics/ui/dialogs
  why: Showing debug popups with AlertDialog.
- url: https://developer.android.com/topic/architecture
  why: Follow recommended separation of concerns.
- file: app/src/main/java/com/example/app/BinLocatorActivity.kt
  why: Existing OCR capture and button patterns.
- file: app/src/main/res/layout/activity_bin_locator.xml
  why: Layout structure for camera preview and action buttons.
- file: app/src/main/res/layout/activity_main.xml
  why: Pattern for main screen widgets.
```

### Current Codebase tree
```bash
./app/src/main/java/com/example/app/
├── BarcodeUtils.kt
├── BinLocatorActivity.kt
├── BoundingBoxOverlay.kt
├── ImageUtils.kt
├── MainActivity.kt
├── OcrParser.kt
├── RecordUploader.kt
└── ZoomUtils.kt
```

### Desired Codebase tree
```bash
app/src/main/res/layout/activity_main.xml          # Adds debug checkbox
app/src/main/java/com/example/app/MainActivity.kt  # Passes debug flag
app/src/main/res/layout/activity_bin_locator.xml   # Adds debug buttons & image
app/src/main/java/com/example/app/BinLocatorActivity.kt # Implements debug logic
app/src/test/java/com/example/app/BinLocatorUnitTest.kt # Tests debug behaviour
app/src/androidTest/java/com/example/app/DebugUiTest.kt  # UI tests
```

### Known Gotchas of our codebase & Library Quirks
```kotlin
// Network operations must not run on the main thread.
// Robolectric tests are ignored in CI; mark with @Ignore.
// Intent extras default to false if missing; handle explicitly.
```

## Implementation Blueprint

### Data models and structure
No new data models. Use an Intent boolean extra "debug" and a `var debugMode`
property in `BinLocatorActivity`.

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  MODIFY app/src/main/res/layout/activity_main.xml:
    - Add CheckBox id=debugCheckBox below the Bin Locator button.

Task 2:
  MODIFY app/src/main/java/com/example/app/MainActivity.kt:
    - Read debugCheckBox.isChecked when starting BinLocatorActivity and pass
      intent extra "debug".

Task 3:
  MODIFY app/src/main/res/layout/activity_bin_locator.xml:
    - Add two small buttons within actionButtons when debug is true:
      showOcrButton and showCropButton.
    - Add ImageView id=cropPreview overlaying previewContainer, initially GONE.

Task 4:
  MODIFY app/src/main/java/com/example/app/BinLocatorActivity.kt:
    - Retrieve intent extra "debug" into debugMode.
    - Store raw OCR lines in a property when capture succeeds.
    - If debugMode hide sendRecordButton and disable RecordUploader calls.
    - showOcrButton opens AlertDialog listing raw lines with bounding box
      heights.
    - showCropButton toggles boundingBox overlay visibility and shows the last
      captured bitmap in cropPreview.

Task 5:
  MODIFY app/src/test/java/com/example/app/BinLocatorUnitTest.kt:
    - Add unit test verifying sendRecordButton stays GONE when debug flag true.

Task 6:
  CREATE app/src/androidTest/java/com/example/app/DebugUiTest.kt:
    - Launch activity with debug extra and assert new buttons are displayed and
      sendRecordButton hidden.

Task 7:
  UPDATE README.md and AppFeatures.txt describing debug mode features.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 2 snippet
val intent = Intent(this, BinLocatorActivity::class.java)
intent.putExtra("debug", debugCheckBox.isChecked)
startActivity(intent)

// Task 4 snippet
override fun onCreate(savedInstanceState: Bundle?) {
    debugMode = intent.getBooleanExtra("debug", false)
    if (debugMode) {
        sendRecordButton.visibility = View.GONE
        debugButtons.visibility = View.VISIBLE
    }
}
```

### Integration Points
```yaml
CONFIG: none
DATABASE: none
ROUTES: none
```

## Validation Loop

### Level 1: Syntax & Style
```bash
./gradlew lint
```

### Level 2: Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Level 3: Instrumentation Test
```bash
./gradlew connectedDebugAndroidTest
```

## Final validation Checklist
- [ ] All tests pass: `./gradlew testDebugUnitTest connectedDebugAndroidTest`
- [ ] No linting errors: `./gradlew lint`
- [ ] README and AppFeatures updated

---

## Anti-Patterns to Avoid
- ❌ Don't run network code on the UI thread.
- ❌ Don't add debug UIs in production builds without guards.
- ❌ Don't skip tests for new branches in logic.

### PRP Confidence Score: 7/10
