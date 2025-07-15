name: "Refined Image to OCR Pipeline"
description: |
  ## Purpose
  Improve cropping accuracy and debugging in the Bin Locator image pipeline. The
  current flow crops using a fixed bounding box and immediately runs text
  recognition. This feature adds a second crop pass based on the label's aspect
  ratio and orientation so only the label area is sent to ML Kit. When debug
  mode is active the cropped image passed to the recogniser is written to a
  temporary file and shown when the **Show Crop** button is pressed.

  ## Core Principles
  1. **Context is King**: reference existing cropping code and ML Kit docs.
  2. **Validation Loops**: Gradle lint plus unit/instrumentation tests.
  3. **Information Dense**: follow Kotlin utility and test patterns in the repo.
  4. **Progressive Success**: implement cropper, tests, then integrate.
  5. **Global rules**: follow CODEX.md guidelines.

---

## Goal
Detect and correct the label region within the initial crop before OCR. Handle
slight rotation and perspective issues and provide better debugging visibility.

## Why
- **Business value**: increases OCR reliability by focusing on the label.
- **Integration**: extends existing `BinLocatorActivity` pipeline and debug UI.
- **Problem solved**: reduces noise from surrounding background and allows easier
  troubleshooting with saved images.

## What
- New `LabelCropper` utility analyses the cropped bitmap for rectangles close to
  the expected label aspect ratio.
- If found, the bitmap is cropped again and optionally deskewed.
- In debug mode the second crop is saved to a temp file; **Show Crop** displays
  this file rather than the raw bitmap.
- The first crop logic remains unchanged to preserve the current overlay box.

### Success Criteria
- [ ] Images are cropped twice when a label candidate is detected.
- [ ] Debug mode saves the second crop and the button displays it from storage.
- [ ] Gradle lint and tests pass.

## All Needed Context

### Documentation & References (list all context needed to implement the feature)
```yaml
- url: https://developer.android.com/topic/architecture
  why: Follow recommended separation of concerns.
- url: https://developers.google.com/ml-kit/vision/text-recognition/android
  why: InputImage and TextRecognition APIs.
- url: https://developer.android.com/reference/android/graphics/Bitmap
  why: Bitmap cropping and rotation utilities.
- url: https://github.com/ryccoatika/Image-To-Text
  why: Example of an image-to-text pipeline.
- file: app/src/main/java/com/example/app/BinLocatorActivity.kt
  why: Existing capture and crop code.
- file: app/src/main/java/com/example/app/ImageUtils.kt
  why: Rotation helper used after capturing.
- file: examples/Image_to_OCR_pipeline.md
  why: Guide on pipeline tuning (referenced in INITIAL.md).
```

### Current Codebase tree (run `tree` in the root of the project) to get an overview of the codebase
```bash
.
├── app/src/main/java/com/example/app
│   ├── BarcodeUtils.kt
│   ├── BatchRecord.kt
│   ├── BinLocatorActivity.kt
│   ├── BoundingBoxOverlay.kt
│   ├── ImageUtils.kt
│   ├── MainActivity.kt
│   ├── OcrParser.kt
│   ├── PinFetcher.kt
│   ├── RecordUploader.kt
│   └── ZoomUtils.kt
├── app/src/test/java/com/example/app
│   ├── BarcodeUtilTest.kt
│   ├── BatchRecordTest.kt
│   ├── BinLocatorUnitTest.kt
│   ├── BoundingBoxUtilTest.kt
│   ├── MainActivityUnitTest.kt
│   ├── OcrParserTest.kt
│   ├── PinFetcherTest.kt
│   ├── RecordUploaderTest.kt
│   └── ZoomUtilTest.kt
├── app/src/androidTest/java/com/example/app
│   ├── BarcodeUiTest.kt
│   ├── BatchUiTest.kt
│   ├── BinLocatorTest.kt
│   ├── BinSelectionUiTest.kt
│   ├── DebugUiTest.kt
│   ├── MainActivityTest.kt
│   ├── SendRecordUiTest.kt
│   └── ZoomUiTest.kt
```

### Desired Codebase tree with files to be added and responsibility of file
```bash
app/src/main/java/com/example/app/LabelCropper.kt          # Finds label region and deskews
app/src/test/java/com/example/app/LabelCropperTest.kt      # Unit tests for cropper
app/src/androidTest/java/com/example/app/ShowCropUiTest.kt # Verify debug crop display
```

### Known Gotchas of our codebase & Library Quirks
```kotlin
// ML Kit text recognition must run off the main thread.
// Bitmap operations can be memory heavy; recycle intermediate bitmaps.
// Robolectric tests are ignored in CI due to missing dependencies.
```

## Implementation Blueprint

### Data models and structure
Create a `LabelCropper` object with a `refineCrop` function returning a new
`Bitmap`. It analyses contours to match the overlay aspect ratio and rotates the
sub-bitmap when necessary.

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  CREATE app/src/main/java/com/example/app/LabelCropper.kt:
    - Function refineCrop(src: Bitmap): Bitmap
    - Search for rectangles near 34:15 aspect ratio.
    - If found, rotate/deskew and return the cropped region; else return src.
    - Ensure memory cleanup of temporary bitmaps.

Task 2:
  MODIFY app/src/main/java/com/example/app/BinLocatorActivity.kt:
    - After initial crop call LabelCropper.refineCrop.
  - In debug mode write the returned bitmap to cacheDir/ocr_debug.png.
    - showCropButton should load this file when toggling preview.

Task 3:
  CREATE app/src/test/java/com/example/app/LabelCropperTest.kt:
    - Use Mockito to create fake bitmaps and verify crop dimensions.
    - Edge case: when no label found the original bitmap is returned.

Task 4:
  CREATE app/src/androidTest/java/com/example/app/ShowCropUiTest.kt:
    - Launch activity in debug mode, trigger capture, and verify the image view
      loads the saved file when Show Crop is pressed.

Task 5:
  UPDATE README.md and AppFeatures.txt documenting the refined OCR pipeline and
  debug behaviour.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 1
object LabelCropper {
    fun refineCrop(src: Bitmap): Bitmap {
        // Identify rectangle matching overlay aspect ratio
        val rect = findCandidateRect(src)
        return if (rect != null) {
            val cropped = Bitmap.createBitmap(src, rect.left, rect.top, rect.width(), rect.height())
            ImageUtils.rotateBitmap(cropped, computeSkewAngle(rect))
        } else src
    }
}
```

### Integration Points
```yaml
ROUTES: none
CONFIG: none
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
- [ ] Debug image saved and shown correctly

---

## Anti-Patterns to Avoid
- ❌ Don't allocate large bitmaps without recycling.
- ❌ Don't run ML Kit or heavy processing on the UI thread.
- ❌ Don't ignore failing tests.

### PRP Confidence Score: 7/10
