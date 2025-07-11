name: "Send Record Button and Database Upload"
description: |
  ## Purpose
  Add a **Send Record** action after OCR parsing so users can upload the
  roll number, customer name and bin to an SQL database via `insert.php`.
  After successful upload the UI clears and confirms with a Snackbar.

  ## Core Principles
  1. **Context is King**: reference existing activity patterns and Android
     networking docs.
  2. **Validation Loops**: use Gradle lint and both unit and instrumentation
     tests.
  3. **Information Dense**: re-use Kotlin patterns already in the project.
  4. **Progressive Success**: build uploader, add tests, then integrate.
  5. **Global rules**: follow repository guidelines in CODEX.md.

---

## Goal
Allow users to transmit the parsed roll number, customer name and bin to
`insert.php` hosted at `unitedexpresstrucking.com`. The activity should show a
**Send Record** button once all three pieces of data are present. Success clears
the view for the next capture.

## Why
- **Business value**: saves scanned records directly to the warehouse database.
- **Integration**: extends existing OCR flow in `BinLocatorActivity`.
- **Problem solved**: currently users must manually record results; this
  automates the upload.

## What
- Add a button to the Bin Locator layout and activity.
- POST data using `HttpURLConnection` on a background thread.
- Snackbar confirms success or failure and resets UI state.
- Unit tests mock the connection; instrumentation tests verify button visibility
  and clearing logic.

### Success Criteria
- [ ] Send Record button appears when roll, customer and bin lines exist.
- [ ] POST request returns HTTP 200 and JSON `{ "status": "success" }`.
- [ ] Snackbar with "Record sent" is shown and OCR text view is cleared.
- [ ] Gradle lint and tests pass.

## All Needed Context

### Documentation & References
```yaml
- url: https://developer.android.com/reference/java/net/HttpURLConnection
  why: Basic API for posting form data.
- url: https://developer.android.com/topic/architecture
  why: Follow recommended separation of concerns.
- url: https://square.github.io/okhttp/recipes/#posting-string-content
  why: Example of building a POST request body.
- file: app/src/main/java/com/example/app/BinLocatorActivity.kt
  why: Existing OCR flow and Snackbar usage.
- file: app/src/main/res/layout/activity_bin_locator.xml
  why: Layout pattern for action buttons.
- file: examples/insert.php
  why: Server-side script handling uploaded records.
- file: app/src/androidTest/java/com/example/app/BinSelectionUiTest.kt
  why: Shows how instrumentation tests manipulate the activity.
```

### Current Codebase tree
```bash
.
├── app/src/main/java/com/example/app/
│   ├── BarcodeUtils.kt
│   ├── BinLocatorActivity.kt
│   ├── BoundingBoxOverlay.kt
│   ├── ImageUtils.kt
│   ├── MainActivity.kt
│   ├── OcrParser.kt
│   └── ZoomUtils.kt
├── app/src/test/java/com/example/app/
│   ├── BarcodeUtilTest.kt
│   ├── BinLocatorUnitTest.kt
│   ├── BoundingBoxUtilTest.kt
│   ├── MainActivityUnitTest.kt
│   ├── OcrParserTest.kt
│   └── ZoomUtilTest.kt
├── app/src/androidTest/java/com/example/app/
│   ├── BarcodeUiTest.kt
│   ├── BinLocatorTest.kt
│   ├── BinSelectionUiTest.kt
│   └── ZoomUiTest.kt
└── examples/insert.php
```

### Desired Codebase tree
```bash
app/src/main/java/com/example/app/RecordUploader.kt       # Handles POST logic
app/src/main/res/layout/activity_bin_locator.xml          # Adds Send Record button
app/src/main/java/com/example/app/BinLocatorActivity.kt    # Integrates uploader
app/src/test/java/com/example/app/RecordUploaderTest.kt    # Unit tests
app/src/androidTest/java/com/example/app/SendRecordUiTest.kt # UI behaviour
```

### Known Gotchas of our codebase & Library Quirks
```kotlin
// Network operations must not run on the main thread.
// Robolectric instrumentation is skipped; mark tests with @Ignore when needed.
// HttpURLConnection requires closing streams to avoid leaks.
```

## Implementation Blueprint

### Data models and structure
Create a `RecordUploader` singleton with a `sendRecord` function that accepts
`roll: String`, `customer: String`, and `bin: String`. It builds an
`HttpURLConnection` POST request to `https://unitedexpresstrucking.com/insert.php`
with URL-encoded form data and parses a JSON response.

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  CREATE app/src/main/java/com/example/app/RecordUploader.kt:
    - Function sendRecord(roll: String, customer: String, bin: String): Boolean
      running on a background executor.
    - POSTs form fields roll_num, customer, bin to insert.php.
    - Returns true on HTTP 200 and status=="success".

Task 2:
  MODIFY app/src/main/res/layout/activity_bin_locator.xml:
    - Add Button id=sendRecordButton inside actionButtons layout after setBinButton.
    - Visibility remains GONE by default.

Task 3:
  MODIFY app/src/main/java/com/example/app/BinLocatorActivity.kt:
    - Declare sendRecordButton and show it in showResult() once three lines exist.
    - Implement sendRecord() that parses lines, calls RecordUploader, then clears
      ocrTextView and hides actionButtons on success.
    - Show Snackbar with confirmation or error message.

Task 4:
  CREATE app/src/test/java/com/example/app/RecordUploaderTest.kt:
    - Mock HttpURLConnection to verify POST payload and success parsing.
    - Test network failure returns false.

Task 5:
  CREATE app/src/androidTest/java/com/example/app/SendRecordUiTest.kt:
    - Invoke showResult via reflection and ensure sendRecordButton becomes visible.
    - Simulate click and check Snackbar text using Espresso.

Task 6:
  UPDATE AppFeatures.txt and README.md describing the new upload capability.

Task 7:
  If insert.php requires adjustment (e.g., new field names), document the change
    in the PR.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 1 pseudocode
object RecordUploader {
    private val executor = Executors.newSingleThreadExecutor()

    fun sendRecord(roll: String, customer: String, bin: String, onComplete: (Boolean) -> Unit) {
        executor.execute {
            try {
                val url = URL("https://unitedexpresstrucking.com/insert.php")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                }
                val body = "roll_num=$roll&customer=$customer&bin=$bin".toByteArray()
                conn.outputStream.use { it.write(body) }
                val success = conn.responseCode == 200 && conn.inputStream.bufferedReader().readText().contains("success")
                onComplete(success)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
}
```

### Integration Points
```yaml
CONFIG:
  - None
DATABASE:
  - `insert.php` expects roll_num, customer and bin POST fields.
ROUTES:
  - None
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
- [ ] insert.php notes documented if modified

---

## Anti-Patterns to Avoid
- ❌ Don't run network code on the UI thread.
- ❌ Don't ignore connection or stream close exceptions.
- ❌ Don't skip tests for error paths.

### PRP Confidence Score: 7/10

