name: "Developer Mode and Preprocess Debug"
description: |
  ## Purpose
  Introduce a developer-only workflow triggered by PIN 8789. Developer mode centralizes all debug and tuning features and exposes a new preprocessing debug screen.

  ## Core Principles
  1. **Context is King**: rely on MainActivity and BinLocator patterns.
  2. **Validation Loops**: Gradle lint plus unit and instrumentation tests.
  3. **Information Dense**: follow existing Kotlin style.
  4. **Progressive Success**: implement login flag, developer UI, then new activity.
  5. **Global rules**: follow CODEX.md guidelines.
---
## Goal
Add a Developer login path that unlocks a Developer screen containing the Debug mode toggle and access to a new Preprocess Debug module. This module displays the OCR preprocessing result immediately without running ML Kit.
## Why
- **Business value**: enables on-device pipeline investigation without releasing debug tools to regular users.
- **Integration**: builds upon existing MainActivity login flow and BinLocatorActivity debug/tune features.
- **Problem solved**: developers need to inspect preprocessing output to tweak parameters.
## What
- Detect PIN 8789 at login and enable developer features.
- Replace the Debug checkbox on the home screen with a Developer button appearing only when in developer mode.
- Developer screen hosts the Debug checkbox and a button launching Preprocess Debug.
- Preprocess Debug captures an image, crops and converts it like Bin Locator but stops before ML Kit, showing the processed image at the top.
- Only Capture and Tune buttons are present in Preprocess Debug.

### Success Criteria
- [ ] Entering PIN 8789 reveals Developer button.
- [ ] Debug checkbox lives inside Developer screen and still passes flag to camera modules.
- [ ] Preprocess Debug shows processed image automatically and omits ML Kit.
- [ ] Gradle lint and tests pass.
## All Needed Context

### Documentation & References (list all context needed to implement the feature)
```yaml
- url: https://developer.android.com/topic/architecture
  why: recommended activity structure and navigation patterns.
- url: https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md
  why: clean architecture best practices.
- file: app/src/main/java/com/example/app/MainActivity.kt
  why: current login flow and intent extras.
- file: app/src/main/java/com/example/app/BinLocatorActivity.kt
  why: debugMode implementation and tuning dialog example.
- file: app/src/main/java/com/example/app/CheckoutActivity.kt
  why: separate activity using same OCR pipeline.
- file: app/src/main/java/com/example/app/TuningParams.kt
  why: runtime tuning parameters referenced by tune dialog.
- file: examples/image_processing/MainActivity.java
  why: example of displaying processed image.
```

### Current Codebase tree (run `tree` in the root of the project) to get an overview of the codebase
```bash
.
├── app
│   └── src
│       ├── main
│       │   ├── java/com/example/app
│       │   └── res
│       ├── test/java/com/example/app
│       └── androidTest/java/com/example/app
```

### Desired Codebase tree with files to be added and responsibility of file
```bash
app/src/main/res/layout/activity_main.xml          # remove debugCheckBox, add developerButton
app/src/main/java/com/example/app/MainActivity.kt  # detect PIN 8789, launch DeveloperActivity
app/src/main/res/layout/activity_developer.xml     # UI with debugCheckBox and preprocessButton
app/src/main/java/com/example/app/DeveloperActivity.kt # handles debug flag and navigation
app/src/main/res/layout/activity_preprocess_debug.xml  # preview plus processed ImageView
app/src/main/java/com/example/app/PreprocessDebugActivity.kt # capture and show preprocessed image
app/src/test/java/com/example/app/DeveloperModeTest.kt  # unit tests for login logic
app/src/androidTest/java/com/example/app/PreprocessDebugUiTest.kt # verify processed image shows
```

### Known Gotchas of our codebase & Library Quirks
```kotlin
// Network operations must not run on the main thread.
// Robolectric tests are ignored in CI; annotate with @Ignore.
// Intent extras default to false if missing; handle explicitly.
```

## Implementation Blueprint

### Data models and structure
No new data models. Introduce `var developerMode = false` in MainActivity and pass a boolean `debug` extra from DeveloperActivity. PreprocessDebugActivity uses existing `TuningParams` for adjustments.

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  MODIFY app/src/main/java/com/example/app/MainActivity.kt:
    - After validating PIN, if pin == "8789" set developerMode = true and show developerButton.
    - Launch DeveloperActivity from developerButton.
    - When starting BinLocatorActivity or CheckoutActivity pass debug flag stored from DeveloperActivity.

Task 2:
  MODIFY app/src/main/res/layout/activity_main.xml:
    - Remove debugCheckBox.
    - Add Button id=developerButton below batchCheckBox, visibility=gone.

Task 3:
  CREATE app/src/main/res/layout/activity_developer.xml:
    - Contains CheckBox id=debugCheckBox and Button id=preprocessButton.

Task 4:
  CREATE app/src/main/java/com/example/app/DeveloperActivity.kt:
    - Manages debugCheckBox state.
    - preprocessButton launches PreprocessDebugActivity.

Task 5:
  CREATE app/src/main/res/layout/activity_preprocess_debug.xml:
    - PreviewView for camera and ImageView id=processedImage on top.
    - Capture and Tune buttons only.

Task 6:
  CREATE app/src/main/java/com/example/app/PreprocessDebugActivity.kt:
    - Replicate camera setup from BinLocatorActivity.
    - After capturing, crop with LabelCropper and convert to grayscale.
    - Display processed bitmap in processedImage without calling ML Kit.

Task 7:
  CREATE app/src/test/java/com/example/app/DeveloperModeTest.kt:
    - Verify developerButton visible only with PIN 8789.
    - Confirm debug flag passed to BinLocatorActivity.

Task 8:
  CREATE app/src/androidTest/java/com/example/app/PreprocessDebugUiTest.kt:
    - Launch activity, trigger capture via reflection, assert processedImage visible.

Task 9:
  UPDATE README.md and AppFeatures.txt documenting developer mode and preprocessing debug.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 1 snippet
if (entered == "8789") {
    developerMode = true
    developerButton.visibility = View.VISIBLE
}

// Task 6 snippet
override fun onImageSaved(results: ImageCapture.OutputFileResults) {
    val bmp = ImageUtils.decodeRotatedBitmap(photoFile)
    val crop = overlay.mapToBitmapRect(bmp.width, bmp.height)
    val warped = LabelCropper.cropLabel(
        Bitmap.createBitmap(bmp, crop.left, crop.top, crop.width(), crop.height()),
        bmp.width * bmp.height
    )
    val processed = ImageUtils.toGrayscale(warped)
    runOnUiThread { processedImage.setImageBitmap(processed) }
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
- ❌ Don't expose developer features without PIN check.
- ❌ Don't run ML Kit in preprocessing debug.
- ❌ Don't skip updating documentation.

### PRP Confidence Score: 7/10
