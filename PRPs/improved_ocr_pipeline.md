name: "Improved OCR Pipeline"
description: |
  ## Purpose
  Enhance the image-to-text pipeline after the initial crop so ML Kit receives a perspective corrected label.
  The overlay crop box still defines the region of interest, but we now detect the label rectangle using its
  aspect ratio and warp it to a flat image before OCR. Debug mode saves the warped image for inspection
  instead of the raw crop.

  ## Core Principles
  1. **Context is King**: reference CameraX capture patterns and OpenCV docs.
  2. **Validation Loops**: Gradle lint plus unit and instrumentation tests.
  3. **Information Dense**: follow Kotlin utility style in the repo.
  4. **Progressive Success**: build cropping utility, integrate, then refine.
  5. **Global rules**: follow repository guidelines in CODEX.md.

---

## Goal
Detect the label within the cropped bitmap, correct perspective based on its aspect ratio, and feed the warped
image to ML Kit. When debug mode is enabled the "Show Crop" button saves and displays this warped image so
developers can inspect it. The pipeline
should tolerate skewed or angled labels and keep performance reasonable on mid‑range devices.

## Why
- **Business value**: More reliable OCR when labels are rotated or not perfectly flat.
- **Integration**: Builds on existing capture flow in `BinLocatorActivity`.
- **Problem solved**: Current OCR fails when the cropped region includes background or angled labels.

## What
- New `LabelCropper` utility using OpenCV to find the best quad matching the known aspect ratio.
- Updated `BinLocatorActivity` to run the cropper before creating `InputImage` and save the result when debugging.
- Additional documentation explaining tuning knobs and pipeline stages.

### Success Criteria
- [ ] Warped bitmap is used for OCR and saved when debug mode is active.
- [ ] Pipeline handles rotated and skewed labels as demonstrated in the examples.
- [ ] Gradle lint and tests pass.
- [ ] README and AppFeatures list the improved pipeline.

## All Needed Context

### Documentation & References (list all context needed to implement the feature)
```yaml
- url: https://developer.android.com/reference/org/opencv/android/OpenCVLoader
  why: Loading the OpenCV native library on Android.
- url: https://docs.opencv.org/4.x/da/d6e/tutorial_py_geometric_transformations.html
  why: Perspective warp and transformation basics.
- url: https://developer.android.com/reference/org/opencv/imgproc/Imgproc#findContours(org.opencv.core.Mat,%20java.util.List,%20org.opencv.core.Mat,%20int,%20int)
  why: Detecting contours used to locate the label rectangle.
- file: examples/Image_to_OCR_pipeline.md
  why: Example pipeline steps with OpenCV API usage.
- file: app/src/main/java/com/example/app/BinLocatorActivity.kt
  why: Existing image capture and OCR flow.
- file: app/src/main/java/com/example/app/BoundingBoxOverlay.kt
  why: Provides the crop box and its aspect ratio.
```

### Current Codebase tree (run `tree` in the root of the project) to get an overview of the codebase
```bash
.
├── app/src/main/java/com/example/app/
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
├── app/src/test/java/com/example/app/
│   ├── BarcodeUtilTest.kt
│   ├── BatchRecordTest.kt
│   ├── BinLocatorUnitTest.kt
│   ├── BoundingBoxUtilTest.kt
│   ├── MainActivityUnitTest.kt
│   ├── OcrParserTest.kt
│   ├── PinFetcherTest.kt
│   ├── RecordUploaderTest.kt
│   └── ZoomUtilTest.kt
└── app/src/androidTest/java/com/example/app/
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
app/src/main/java/com/example/app/LabelCropper.kt    # OpenCV label detection and perspective warp
app/src/test/java/com/example/app/LabelCropperTest.kt # Unit tests for cropper logic (ignored if OpenCV not present)
pipeline-explained.txt                               # Step-by-step description of new pipeline
```

### Known Gotchas of our codebase & Library Quirks
```kotlin
// OpenCV requires calling OpenCVLoader.initDebug() before use.
// ML Kit InputImage expects upright bitmaps; ensure rotation is handled.
// Robolectric tests for OpenCV are skipped in CI using @Ignore.
```

## Implementation Blueprint

### Data models and structure
`LabelCropper` will expose a single function:
```kotlin
object LabelCropper {
    fun cropLabel(bitmap: Bitmap, aspect: Float): Bitmap
}
```
It returns the perspective-corrected label or the original bitmap if no suitable quad is found.

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  MODIFY app/build.gradle:
    - Add dependency "implementation 'ai.eye2you:opencv-android:4.5.2'".

Task 2:
  CREATE app/src/main/java/com/example/app/LabelCropper.kt:
    - Use OpenCV to convert the bitmap to a Mat.
    - Detect contours and approximate polygons.
    - Choose the quad closest to the expected aspect ratio from BoundingBoxOverlay.
    - Warp the perspective to a fixed size and return as Bitmap.

Task 3:
  CREATE pipeline-explained.txt in project root:
    - Document each stage: grayscale, blur, Canny, dilate, contour filtering, warp, ML Kit input.

Task 4:
  MODIFY app/src/main/java/com/example/app/BinLocatorActivity.kt:
    - After cropping via BoundingBoxOverlay, call LabelCropper.cropLabel.
    - Save the warped bitmap to a temporary file when debugMode is true.
    - Use the warped bitmap to create InputImage for OCR and barcode scanning.
    - Update toggleCropPreview() to load the saved warped image.

Task 5:
  CREATE app/src/test/java/com/example/app/LabelCropperTest.kt:
    - Basic test verifying cropLabel returns a non-empty bitmap when given a sample image.
    - Mark with @Ignore since OpenCV native libs not in CI.

Task 6:
  UPDATE README.md and AppFeatures.txt summarizing the improved pipeline and debug behaviour.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 2 snippet
fun cropLabel(bitmap: Bitmap, aspect: Float): Bitmap {
    val mat = Mat()
    Utils.bitmapToMat(bitmap, mat)
    // preprocess: gray, blur, edges, dilate
    // findContours -> approxPolyDP
    // choose bestQuad by aspect ratio
    // warpPerspective to LABEL_W x LABEL_H
    // convert back to Bitmap
    return result
}

// Task 4 snippet
val crop = overlay.mapToBitmapRect(rotated.width, rotated.height)
val cropped = Bitmap.createBitmap(rotated, crop.left, crop.top, crop.width(), crop.height())
val warped = LabelCropper.cropLabel(cropped, overlay.aspectRatio())
lastBitmap = warped
if (debugMode) File(cacheDir, "warped.jpg").also { outFile ->
    warped.compress(Bitmap.CompressFormat.JPEG, 90, FileOutputStream(outFile))
}
val inputImage = InputImage.fromBitmap(warped, 0)
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
- [ ] Pipeline steps documented in `pipeline-explained.txt`
- [ ] README and AppFeatures updated

---

## Anti-Patterns to Avoid
- ❌ Don't process images on the UI thread.
- ❌ Don't ignore OpenCV initialization errors.
- ❌ Don't skip debug image cleanup to avoid storage bloat.

### PRP Confidence Score: 7/10
