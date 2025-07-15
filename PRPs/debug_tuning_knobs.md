name: "Debug Tuning Knobs"
description: |
  ## Purpose
  Provide a menu in debug mode exposing tuning knobs for the OCR pipeline.
  Values are adjustable at runtime but reset on app restart.

  ## Core Principles
  1. **Context is King**: reference pipeline_tuning_guide and existing debug patterns.
  2. **Validation Loops**: Gradle lint plus unit/instrumentation tests.
  3. **Information Dense**: follow Kotlin activity and dialog styles.
  4. **Progressive Success**: implement menu, apply values, then refine.
  5. **Global rules**: follow CODEX.md guidelines.

---

## Goal
Allow adjusting OCR parsing and preprocessing parameters through a debug menu. The menu lists seven knobs from `examples/pipeline_tuning_guide.md` plus a new "percent height of tallest line" option.

## Why
- **Business value**: enables rapid experimentation to improve recognition accuracy.
- **Integration**: extends existing Debug mode UI in `BinLocatorActivity`.
- **Problem solved**: developers can tweak thresholds without rebuilding the app.

## What
- Button opens a dialog listing all tuning knobs with sliders or numeric fields.
- Changes apply only while the app is running in debug mode.
- Values revert to defaults on restart.

### Success Criteria
- [ ] Debug menu displays eight adjustable values.
- [ ] Parser and cropper use updated values when debug mode active.
- [ ] Gradle lint and tests pass.

## All Needed Context

### Documentation & References (list all context needed to implement the feature)
```yaml
- url: https://developer.android.com/topic/architecture
  why: Follow recommended separation of concerns.
- url: https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md
  why: Example of clean architecture in Android apps.
- file: app/src/main/java/com/example/app/BinLocatorActivity.kt
  why: Existing debug buttons and OCR pipeline.
- file: app/src/main/java/com/example/app/OcrParser.kt
  why: Current line filtering logic (75% tallest line).
- file: examples/pipeline_tuning_guide.md
  why: Defines the seven tuning knobs and expected defaults.
```

### Current Codebase tree (run `tree` in the root of the project) to get an overview of the codebase
```bash
./app/src/main/java/com/example/app/
├── BarcodeUtils.kt
├── BatchRecord.kt
├── BinLocatorActivity.kt
├── BoundingBoxOverlay.kt
├── DebugLogger.kt
├── ImageUtils.kt
├── LabelCropper.kt
├── MainActivity.kt
├── OcrParser.kt
├── PinFetcher.kt
├── RecordUploader.kt
└── ZoomUtils.kt
```

### Desired Codebase tree with files to be added and responsibility of file
```bash
app/src/main/java/com/example/app/DebugTuning.kt        # Holds current knob values
app/src/main/res/layout/tuning_dialog.xml               # Dialog layout with sliders/inputs
app/src/main/java/com/example/app/BinLocatorActivity.kt  # Adds Tuning button and applies values
app/src/test/java/com/example/app/DebugTuningTest.kt     # Unit tests for knob handling
app/src/androidTest/java/com/example/app/TuningUiTest.kt # UI test for dialog visibility
```

### Known Gotchas of our codebase & Library Quirks
```kotlin
// Robolectric tests are ignored in CI.
// Values should not persist; store in a singleton or ViewModel not SharedPreferences.
// Dialogs must run on the UI thread.
```

## Implementation Blueprint

### Data models and structure
Create a `data class TuningOptions` containing each knob as a mutable property.
`DebugTuning` singleton stores the current `TuningOptions` and provides defaults read from the guide.

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  CREATE app/src/main/java/com/example/app/DebugTuning.kt:
    - data class TuningOptions(heightPercent: Float = 0.75f, ...other six knobs...)
    - object DebugTuning { var options = TuningOptions() }

Task 2:
  MODIFY app/src/main/res/layout/activity_bin_locator.xml:
    - Add Button id=tuningButton below showLogButton, visibility GONE by default.
    - Create tuning_dialog.xml with Slider elements for each knob.

Task 3:
  MODIFY app/src/main/java/com/example/app/BinLocatorActivity.kt:
    - When debugMode true show tuningButton.
    - tuningButton opens AlertDialog using tuning_dialog layout.
    - Updates DebugTuning.options when sliders change.
    - Apply options.heightPercent in OcrParser and other knobs per guide.

Task 4:
  MODIFY app/src/main/java/com/example/app/OcrParser.kt:
    - Replace hardcoded 0.75 threshold with DebugTuning.options.heightPercent.

Task 5:
  MODIFY LabelCropper or other pipeline utilities to use remaining knob values as documented.

Task 6:
  CREATE app/src/test/java/com/example/app/DebugTuningTest.kt:
    - Verify options reset to defaults on new instance and update correctly.

Task 7:
  CREATE app/src/androidTest/java/com/example/app/TuningUiTest.kt:
    - Launch activity in debug mode, open tuning dialog, adjust a value and confirm effect on OCR filtering.

Task 8:
  UPDATE README.md and AppFeatures.txt documenting the tuning menu.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 3 snippet
val dialogView = layoutInflater.inflate(R.layout.tuning_dialog, null)
val heightSlider = dialogView.findViewById<Slider>(R.id.heightSlider)
heightSlider.value = DebugTuning.options.heightPercent * 100
heightSlider.addOnChangeListener { _, value, _ ->
    DebugTuning.options = DebugTuning.options.copy(heightPercent = value / 100f)
}
AlertDialog.Builder(this)
    .setTitle("Tuning")
    .setView(dialogView)
    .setPositiveButton("Done", null)
    .show()
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
- ❌ Don't store tuning values in persistent storage.
- ❌ Don't skip updating parser when options change.
- ❌ Don't ignore UI thread requirements for dialogs.

### PRP Confidence Score: 7/10
