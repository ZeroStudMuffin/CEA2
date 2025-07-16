name: "Debug Mode Tuning Menu"
description: |
  ## Purpose
  Add a tuning menu in debug mode letting developers adjust key OCR pipeline
  parameters at runtime. This aids experimentation without recompiling the
  app.

  ## Core Principles
  1. **Context is King**: include references to existing pipeline code and
     tuning guide.
  2. **Validation Loops**: use Gradle lint and tests.
  3. **Information Dense**: follow Kotlin UI and dialog patterns already in
     the app.
  4. **Progressive Success**: start with data model, wire up UI, then apply
     values in pipeline.
  5. **Global rules**: follow repository guidelines in CODEX.md.

---

## Goal
Expose sliders and fields in Bin Locator debug mode for adjusting
pipeline parameters derived from `examples/pipeline_tuning_guide.md`.
Changes apply only for the current session and affect OCR and parsing
behaviour immediately.

## Why
- **Business value**: simplifies experimentation with OCR preprocessing,
  enabling better accuracy tuning on-device.
- **Integration**: extends existing debug mode in `BinLocatorActivity`.
- **Problem solved**: recompiling to tweak constants slows iteration.

## What
- Add a new "Tune Pipeline" button visible only in debug mode.
- Display a dialog listing each knob with sliders or text inputs.
- Apply chosen values to `LabelCropper` and `OcrParser` for the session
  only (no persistence).
- Include "percent height of tallest line" as an extra knob for
  `OcrParser` filtering.

### Success Criteria
- [ ] Tune Pipeline button opens a dialog with all knobs labelled.
- [ ] Parameters update debug-mode pipeline instantly.
- [ ] Values reset to defaults when app restarts.
- [ ] Gradle lint and tests pass.

## All Needed Context

### Documentation & References
```yaml
- file: examples/pipeline_tuning_guide.md
  why: Describes the seven pipeline parameters and suggested values.
- file: app/src/main/java/com/example/app/LabelCropper.kt
  why: Contains default preprocessing constants to be parameterised.
- file: app/src/main/java/com/example/app/OcrParser.kt
  why: Filtering uses a 75% height rule; will become configurable.
- file: app/src/main/java/com/example/app/BinLocatorActivity.kt
  why: Shows current debug UI and button patterns.
- url: https://developer.android.com/topic/architecture
  why: Guide for structuring data classes and state handling.
- url: https://github.com/ryccoatika/Image-To-Text
  why: Example app with runtime parameter adjustments.
```

### Current Codebase tree
```bash
app/src/main/java/com/example/app
├── BarcodeUtils.kt
├── BatchRecord.kt
├── BinLocatorActivity.kt
├── BoundingBoxOverlay.kt
├── ImageUtils.kt
├── LabelCropper.kt
├── MainActivity.kt
├── OcrParser.kt
├── PinFetcher.kt
├── RecordUploader.kt
└── ZoomUtils.kt
app/src/test/java/com/example/app
├── BarcodeUtilTest.kt
├── BatchRecordTest.kt
├── BinLocatorUnitTest.kt
├── BoundingBoxUtilTest.kt
├── ImageUtilsTest.kt
├── LabelCropperTest.kt
├── MainActivityUnitTest.kt
├── OcrParserTest.kt
├── PinFetcherTest.kt
├── RecordUploaderTest.kt
└── ZoomUtilTest.kt
app/src/androidTest/java/com/example/app
├── BarcodeUiTest.kt
├── BatchUiTest.kt
├── BinLocatorTest.kt
├── BinSelectionUiTest.kt
├── DebugUiTest.kt
├── MainActivityTest.kt
├── SendRecordUiTest.kt
└── ZoomUiTest.kt
```

### Desired Codebase tree with files to be added and responsibility of file
```bash
app/src/main/java/com/example/app/TuningParams.kt        # Data class holding runtime values
app/src/main/res/layout/dialog_tuning.xml                # Sliders/text fields for knobs
app/src/main/java/com/example/app/BinLocatorActivity.kt   # Shows button and applies params
app/src/main/java/com/example/app/LabelCropper.kt         # Uses current TuningParams
app/src/main/java/com/example/app/OcrParser.kt            # Uses current TuningParams
app/src/test/java/com/example/app/TuningParamsTest.kt     # Unit tests for defaults
app/src/test/java/com/example/app/BinLocatorTuningTest.kt # Verify UI updates values
```

### Known Gotchas of our codebase & Library Quirks
```kotlin
// OpenCV requires OpenCVLoader.initDebug() before use.
// Robolectric tests are ignored using @Ignore in CI.
// Intent extras default to false if missing; handle explicitly.
```

## Implementation Blueprint

### Data models and structure
Create a singleton `TuningParams` object storing the knob values with
sane defaults from the guide.
```kotlin
object TuningParams {
    var blurKernel: Int = 5
    var cannyLow: Int = 50
    var cannyHigh: Int = 150
    var dilateKernel: Int = 3
    var epsilon: Double = 10.0
    var minAreaRatio: Float = 0.1f
    var ratioTolerance: Float = 0.1f
    var outputWidth: Int = 800
    var outputHeight: Int = 200
    var lineHeightPercent: Float = 0.75f // percent height of tallest line
}
```

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  CREATE app/src/main/java/com/example/app/TuningParams.kt:
    - Define singleton as above with defaults.

Task 2:
  CREATE app/src/main/res/layout/dialog_tuning.xml:
    - Use SeekBar or Slider for numeric knobs and EditText for sizes.
    - Include "Apply" button.

Task 3:
  MODIFY app/src/main/java/com/example/app/BinLocatorActivity.kt:
    - When debugMode true, show new Tune button.
    - On click inflate dialog_tuning.xml and update TuningParams on Apply.

Task 4:
  MODIFY app/src/main/java/com/example/app/LabelCropper.kt:
    - Replace hardcoded constants with values from TuningParams.
    - Use lineHeightPercent to filter contours via OcrParser.

Task 5:
  MODIFY app/src/main/java/com/example/app/OcrParser.kt:
    - Make height threshold calculation use TuningParams.lineHeightPercent.

Task 6:
  CREATE app/src/test/java/com/example/app/TuningParamsTest.kt:
    - Assert defaults match guide values.

Task 7:
  CREATE app/src/test/java/com/example/app/BinLocatorTuningTest.kt:
    - Robolectric test (ignored) checking Tune button updates parameters.

Task 8:
  UPDATE README.md and AppFeatures.txt describing tuning capability.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 3 snippet
val view = layoutInflater.inflate(R.layout.dialog_tuning, null)
AlertDialog.Builder(this)
    .setTitle("Tune Pipeline")
    .setView(view)
    .setPositiveButton("Apply") { _, _ ->
        TuningParams.blurKernel = view.blurSlider.value.toInt()
        // ...set remaining params...
    }
    .setNegativeButton("Cancel", null)
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
- ❌ Don't run heavy OpenCV operations on the UI thread.
- ❌ Don't persist tuning values across sessions.
- ❌ Don't ignore missing permissions for camera/storage access.

### PRP Confidence Score: 7/10
