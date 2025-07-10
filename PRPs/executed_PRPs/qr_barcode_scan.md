name: "QR & Barcode Scan Integration"
description: |
  ## Purpose
  Extend the Bin Locator with the ability to parse barcodes and QR codes from the
  captured image. Display OCR text in a text box instead of a popup and provide
  buttons to extract a release number and bin location using ML Kit's barcode
  scanning.

  ## Core Principles
  1. **Context is King**: include docs for ML Kit barcode scanning and existing
     activity patterns.
  2. **Validation Loops**: use Gradle tasks for linting and tests.
  3. **Information Dense**: reference current files for OCR and UI logic.
  4. **Progressive Success**: first show text in TextView, then add scanning
     buttons.
  5. **Global rules**: follow repository guidelines in CODEX.md.

---

## Goal
Display recognized text in a persistent TextView and allow users to extract
release numbers or bin locations via dedicated buttons using barcode scanning.

## Why
- **Business value**: streamlines recording release numbers and bin locations
  from printed labels.
- **Integration**: builds on BinLocatorActivity without altering existing OCR
  workflow.
- **Problem solved**: avoids manual data entry and reduces errors when scanning
  shipment documents.

## What
- Replace the alert dialog with a TextView at the top of the screen showing OCR
  results.
- Show two buttons after OCR: **Get Release** and **Set Bin**.
- "Get Release" searches detected barcodes for a 7‑digit string; show temporary
  "no release found" message if absent.
- "Set Bin" searches for QR codes matching `BIN:<#> UNTIED EXPRESS`; show
  "no bin found" if pattern missing.

### Success Criteria
- [ ] TextView updates with OCR results.
- [ ] Buttons appear after text is displayed and trigger barcode scanning.
- [ ] Release number and bin location extracted correctly when present.
- [ ] Gradle lint, unit tests and instrumentation tests pass.

## All Needed Context

### Documentation & References
```yaml
- url: https://developer.android.com/ml-kit/vision/barcode-scanning
  why: official guide for BarcodeScanning API usage.
- url: https://developers.google.com/ml-kit/vision/barcode-scanning/android#kotlin
  why: dependency and setup instructions.
- url: https://developer.android.com/reference/com/google/android/material/snackbar/Snackbar
  why: show short messages when data not found.
- file: app/src/main/java/com/example/app/BinLocatorActivity.kt
  why: existing camera capture and OCR logic to extend.
- file: app/src/main/res/layout/activity_bin_locator.xml
  why: layout to modify with TextView and new buttons.
```

### Current Codebase tree
```bash
$(tree -L 2 | head -n 20)
```

### Desired Codebase tree with files to be added and responsibility of file
```bash
.
├── app/src/main/java/com/example/app/BarcodeUtils.kt          # Parse barcodes
├── app/src/main/java/com/example/app/BinLocatorActivity.kt    # Update UI and scanning logic
├── app/src/main/res/layout/activity_bin_locator.xml           # Add TextView and buttons
├── app/src/test/java/com/example/app/BarcodeUtilTest.kt       # Unit tests for extraction
├── app/src/androidTest/java/com/example/app/BarcodeUiTest.kt  # UI test for buttons
```

### Known Gotchas of our codebase & Library Quirks
```kotlin
// ML Kit barcode scanning requires dependency 'com.google.mlkit:barcode-scanning'.
// Snackbar requires a view (e.g. PreviewView) for show().
// Unhandled ML Kit tasks must close image resources to avoid memory leaks.
```

## Implementation Blueprint

### Data models and structure
Create `BarcodeUtils` with two functions:
```kotlin
fun extractRelease(barcodes: List<Barcode>): String? // returns 7-digit code
fun extractBin(barcodes: List<Barcode>): String?     // returns BIN number
```
No persistent models are added.

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  MODIFY app/build.gradle:
    - Add dependency "com.google.mlkit:barcode-scanning:17.2.0".

Task 2:
  MODIFY app/src/main/res/layout/activity_bin_locator.xml:
    - Add TextView at the top for OCR results.
    - Add two Buttons (getReleaseButton, setBinButton) below the TextView,
      initially visibility="gone".

Task 3:
  CREATE app/src/main/java/com/example/app/BarcodeUtils.kt:
    - Provide extractRelease() and extractBin() using regex on Barcode.rawValue.

Task 4:
  MODIFY app/src/main/java/com/example/app/BinLocatorActivity.kt:
    - Reference the new TextView and buttons.
    - Replace showResult() dialog with setting text on the TextView and
      making buttons visible.
    - Implement onClick handlers calling BarcodeScanning.getClient().process()
      on the captured bitmap.
    - Use BarcodeUtils to parse results and update record or show Snackbar with
      "no release found" / "no bin found".

Task 5:
  CREATE app/src/test/java/com/example/app/BarcodeUtilTest.kt:
    - Test extraction functions for valid barcode, invalid code, and QR pattern edge cases.

Task 6:
  CREATE app/src/androidTest/java/com/example/app/BarcodeUiTest.kt:
    - Verify buttons become visible after OCR and Snackbar messages appear when
      no barcode or QR code is found.

Task 7:
  UPDATE AppFeatures.txt and README.md documenting barcode/QR scanning.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 3 pseudocode
object BarcodeUtils {
    private val releaseRegex = Regex("\b\d{7}\b")
    private val binRegex = Regex("BIN:(\d+) UNTIED EXPRESS")

    fun extractRelease(list: List<Barcode>): String? =
        list.mapNotNull { it.rawValue }
            .firstOrNull { releaseRegex.containsMatchIn(it) }
            ?.let { releaseRegex.find(it)?.value }

    fun extractBin(list: List<Barcode>): String? =
        list.mapNotNull { it.rawValue }
            .firstOrNull { binRegex.containsMatchIn(it) }
            ?.let { binRegex.find(it)?.groupValues?.get(1) }
}
```

### Integration Points
```yaml
GRADLE:
  - app/build.gradle: barcode-scanning dependency.
CONFIG:
  - none.
ROUTES:
  - MainActivity unchanged; BinLocatorActivity updated internally.
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
- [ ] OCR text shown in TextView with buttons visible
- [ ] Barcode scanning extracts release/bin correctly
- [ ] Documentation updated with new feature

## Anti-Patterns to Avoid
- ❌ Do not keep AlertDialog; results must appear in TextView.
- ❌ Do not ignore failed ML Kit tasks or forget to show user feedback.
- ❌ Do not exceed 500 lines in any file; split into utils if needed.

### PRP Confidence Score: 7/10

