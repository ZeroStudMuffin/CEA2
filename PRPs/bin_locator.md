name: "Bin Locator Feature"
description: |
  ## Purpose
  Implement a bin locator feature that captures an image, preprocesses it for ML Kit text recognition, and displays the recognized text. Supports landscape/portrait with manual rotation and bounding box crop overlay.

  ## Core Principles
  1. **Context is King**: include essential docs and examples for Android camera and ML Kit OCR.
  2. **Validation Loops**: provide gradle commands for linting, building, and tests.
  3. **Information Dense**: reference code patterns from existing project.
  4. **Progressive Success**: start simple, validate, then enhance.
  5. **Global rules**: follow repository guidelines in CODEX.md.

---

## Goal
Create a screen accessible from the main activity with a button to launch the Bin Locator. Users can capture a photo, adjust orientation, crop with bounding box, and run ML Kit OCR to show detected text in a dialog.

## Why
- **Business value**: enables locating bin numbers via device camera.
- **Integration**: builds on current Android skeleton with additional activity and ML Kit dependency.
- **Problem solved**: automates manual entry of bin information using OCR.

## What
- Add navigation from MainActivity to new BinLocatorActivity.
- Camera preview with manual rotation toggle and 70% landscape crop box.
- Capture button performs ML Kit text recognition and displays result.

### Success Criteria
- [ ] App builds and runs with new activity.
- [ ] Camera preview rotates correctly with manual button.
- [ ] Captured text displayed in popup using ML Kit OCR.
- [ ] Unit and instrumentation tests cover orientation logic and OCR result handling.

## All Needed Context

### Documentation & References
```yaml
- url: https://developer.android.com/topic/architecture
  why: guidance on recommended Android architecture.
- url: https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md
  why: reference implementation patterns.
- url: https://developer.android.com/ml-kit/vision/text-recognition
  why: official ML Kit Text Recognition usage.
- file: app/src/main/java/com/example/app/MainActivity.kt
  why: pattern for activity creation and layout inflation.
- file: app/src/androidTest/java/com/example/app/MainActivityTest.kt
  why: example Espresso test structure.
```

### Current Codebase tree
```bash
$(tree -L 2)
```

### Desired Codebase tree with files to be added and responsibility of file
```bash
.
├── app/src/main/java/com/example/app/BinLocatorActivity.kt     # Activity handling camera preview and OCR
├── app/src/main/res/layout/activity_bin_locator.xml            # Layout with preview, bounding box, buttons
├── app/src/main/res/drawable/ic_rotate.xml                     # Icon for rotate button
├── app/src/androidTest/java/com/example/app/BinLocatorTest.kt   # UI test for capture flow
├── app/src/test/java/com/example/app/BinLocatorUnitTest.kt      # Unit tests for orientation utilities
```

### Known Gotchas of our codebase & Library Quirks
```kotlin
// ML Kit requires Google ML Kit dependency in build.gradle.
// CameraX analysis requires closing ImageProxy after processing.
// Instrumentation tests need emulator with camera enabled.
```

## Implementation Blueprint

### Data models and structure
Image capture handled via CameraX. Orientation utilities convert sensor orientation. No persistent data models required.

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  MODIFY app/build.gradle:
    - Add ML Kit Text Recognition and CameraX dependencies.

Task 2:
  CREATE app/src/main/java/com/example/app/BinLocatorActivity.kt:
    - Set up CameraX preview with manual rotation toggle.
    - Overlay bounding box covering ~70% in landscape.
    - Capture button obtains image, rotates as needed, runs TextRecognizer.
    - Show results in AlertDialog.

Task 3:
  CREATE activity_bin_locator.xml layout with PreviewView, overlay bounding box, capture and rotate buttons.

Task 4:
  CREATE orientation utilities in same file or helper class with unit tests.

Task 5:
  CREATE BinLocatorUnitTest.kt verifying rotation math.

Task 6:
  CREATE BinLocatorTest.kt instrumentation test ensuring text appears after capture (mock recognizer if needed).

Task 7:
  MODIFY MainActivity.kt to add button navigating to BinLocatorActivity.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 2 pseudocode snippet
class BinLocatorActivity : AppCompatActivity() {
    private lateinit var cameraProvider: ProcessCameraProvider
    private var rotation: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bin_locator)
        // setup camera and buttons
    }

    private fun analyzeImage(image: ImageProxy) {
        val rotated = rotateBitmap(image.toBitmap(), rotation)
        val inputImage = InputImage.fromBitmap(rotated, 0)
        recognizer.process(inputImage)
            .addOnSuccessListener { texts -> showResult(texts.text) }
            .addOnFailureListener { showError(it) }
            .addOnCompleteListener { image.close() }
    }
}
```

### Integration Points
```yaml
GRADLE:
  - app/build.gradle dependencies for ML Kit and CameraX.
ROUTES:
  - MainActivity launches BinLocatorActivity via Intent.
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

### Level 3: Instrumentation Tests
```bash
./gradlew connectedDebugAndroidTest
```

## Final validation Checklist
- [ ] All tests pass: `./gradlew testDebugUnitTest connectedDebugAndroidTest`
- [ ] No linting errors: `./gradlew lint`
- [ ] Manual capture flow works on emulator
- [ ] Dialog shows recognized text
- [ ] Orientation toggle works in both modes

## Anti-Patterns to Avoid
- ❌ Don't ignore ImageProxy.close leading to memory leak.
- ❌ Don't rely on auto-rotate; always use manual rotation button.
- ❌ Don't leave unhandled exceptions from ML Kit tasks.

### PRP Confidence Score: 7/10

