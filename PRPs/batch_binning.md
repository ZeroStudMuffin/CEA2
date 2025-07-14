name: "Batch Binning Mode"
description: |
  ## Purpose
  Allow users to capture multiple roll/customer pairs before assigning a bin and uploading them. Batch mode adds an "Add Item" flow so several records can be queued and sent together.

  ## Core Principles
  1. **Context is King**: reference existing activity patterns and Android docs.
  2. **Validation Loops**: Gradle lint plus unit/instrumentation tests.
  3. **Information Dense**: reuse Kotlin utility styles in the repo.
  4. **Progressive Success**: build data model, queue logic, then integrate.
  5. **Global rules**: follow CODEX.md guidelines.

---

## Goal
Enable a "Batch Binning" checkbox on the main screen. When active the Bin Locator allows multiple captures to be added to a list via an **Add Item** button. Setting the bin applies to all queued items and clicking **Send Record** uploads each item individually then resets the list.

## Why
- **Business value**: speeds up scanning by grouping captures before selecting a bin.
- **Integration**: extends existing BinLocatorActivity and RecordUploader flows.
- **Problem solved**: removes repetitive bin selection when scanning many rolls.

## What
- Additional checkbox toggles batch mode.
- Capture button shows a square icon in batch mode.
- New Add Item button queues the current roll/customer pair and clears the view.
- A Show Items popup lists queued records.
- Set Bin updates all queued items and the current capture.
- Send Record iterates over the queue sending each item.

### Success Criteria
- [ ] Batch checkbox launches Bin Locator with batch mode extra.
- [ ] Add Item stores roll and customer and clears text view.
- [ ] Show Items dialog lists queued pairs.
- [ ] Set Bin applies the chosen bin to all queued items and current view.
- [ ] Send Record uploads each queued item and resets state.
- [ ] Gradle lint and tests pass.

## All Needed Context

### Documentation & References
```yaml
- url: https://developer.android.com/guide/topics/ui/controls/checkbox
  why: Implementing CheckBox UI element.
- url: https://developer.android.com/guide/topics/ui/dialogs
  why: Showing the queued item list in an AlertDialog.
- url: https://developer.android.com/topic/architecture
  why: Follow recommended separation of concerns.
- file: app/src/main/java/com/example/app/BinLocatorActivity.kt
  why: Existing capture, bin and send logic.
- file: app/src/main/res/layout/activity_bin_locator.xml
  why: Layout pattern for action buttons and capture UI.
- file: app/src/main/java/com/example/app/MainActivity.kt
  why: Example of passing intent extras for debug mode.
- file: app/src/main/res/layout/activity_main.xml
  why: Layout for debug checkbox to mirror batch checkbox.
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
├── PinFetcher.kt
├── RecordUploader.kt
└── ZoomUtils.kt
```

### Desired Codebase tree
```bash
app/src/main/res/layout/activity_main.xml            # Adds batchCheckBox
app/src/main/java/com/example/app/MainActivity.kt    # Passes batch flag
app/src/main/res/layout/activity_bin_locator.xml     # Add addItemButton and showBatchButton
app/src/main/java/com/example/app/BinLocatorActivity.kt # Batch logic and list handling
app/src/main/java/com/example/app/BatchRecord.kt     # Data class for queued items
app/src/test/java/com/example/app/BatchRecordTest.kt # Unit tests
app/src/androidTest/java/com/example/app/BatchUiTest.kt # UI tests
```

### Known Gotchas of our codebase & Library Quirks
```kotlin
// Network operations must not run on the main thread.
// Robolectric tests are ignored in CI; annotate with @Ignore.
// Intent extras default to false if missing; handle explicitly.
```

## Implementation Blueprint

### Data models and structure
Create a simple `data class BatchRecord(var roll: String, var customer: String, var bin: String? = null)` stored in a mutable list inside `BinLocatorActivity` when batch mode is enabled.

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  MODIFY app/src/main/res/layout/activity_main.xml:
    - Add CheckBox id=batchCheckBox above debugCheckBox.

Task 2:
  MODIFY app/src/main/java/com/example/app/MainActivity.kt:
    - Read batchCheckBox.isChecked and pass intent extra "batch" when launching BinLocatorActivity.

Task 3:
  MODIFY app/src/main/res/layout/activity_bin_locator.xml:
    - Add Button id=addItemButton to the right of captureButton.
    - Add Button id=showBatchButton within actionButtons, visibility GONE by default.

Task 4:
  CREATE app/src/main/java/com/example/app/BatchRecord.kt:
    - Defines BatchRecord data class.

Task 5:
  MODIFY app/src/main/java/com/example/app/BinLocatorActivity.kt:
    - Add batchMode boolean from intent extra and list<BatchRecord> batchItems.
    - When batchMode true change captureButton size to 48dp square.
    - addItemButton saves current roll/customer to batchItems and clears ocrTextView.
    - showBatchButton displays AlertDialog listing batchItems.
    - applyBin() applies bin to current view and all batchItems.
    - sendRecord() iterates over batchItems plus current item and calls RecordUploader for each; clear list after.

Task 6:
  CREATE app/src/test/java/com/example/app/BatchRecordTest.kt:
    - Test addItem stores values and clears view.
    - Test applyBin updates all records.

Task 7:
  CREATE app/src/androidTest/java/com/example/app/BatchUiTest.kt:
    - Launch activity with batch extra and verify addItemButton visible and sendRecordButton updates after adding items.

Task 8:
  UPDATE README.md and AppFeatures.txt documenting batch binning.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 5 snippet
class BinLocatorActivity : AppCompatActivity() {
    private var batchMode = false
    private val batchItems = mutableListOf<BatchRecord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        batchMode = intent.getBooleanExtra("batch", false)
        if (batchMode) {
            captureButton.width = 48
            captureButton.height = 48
            addItemButton.visibility = View.VISIBLE
            showBatchButton.visibility = View.VISIBLE
        }
    }

    private fun onAddItem() {
        val roll = ... // parse from ocrTextView
        val cust = ...
        batchItems += BatchRecord(roll, cust)
        ocrTextView.text = ""
    }
```

### Integration Points
```yaml
CONFIG: none
DATABASE: existing insert.php via RecordUploader
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
- ❌ Don't skip queue clearing after send.
- ❌ Don't forget to hide batch UI when not in batch mode.

### PRP Confidence Score: 7/10
