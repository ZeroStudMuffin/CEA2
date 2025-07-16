name: "Batch Mode Toggle & Bin Menu"
description: |
  ## Purpose
  Reintroduce two operational modes for bin selection. In default mode a
  full-screen grid of bin buttons appears automatically once the roll and
  customer lines are parsed. Batch mode retains the existing Add Item and
  Show Items workflow. Users toggle batch mode from the main screen.

  ## Core Principles
  1. **Context is King**: reference current activity and layout patterns.
  2. **Validation Loops**: Gradle lint plus unit and instrumentation tests.
  3. **Information Dense**: follow Kotlin and XML conventions in the repo.
  4. **Progressive Success**: implement checkbox, overlay menu, then tests.
  5. **Global rules**: follow repository guidelines in CODEX.md.

---

## Goal
Allow users to choose between single‑record mode with an automatic bin
menu and batch mode with item queuing. In default (non‑batch) mode the
app pops up a "BINS" overlay once a roll and customer are detected,
covering the screen except for the capture button. Selecting any bin
sets `BIN=<value>`, uploads the record and clears the text view.

## Why
- **Business value**: streamlines single item scanning by reducing taps.
- **Integration**: builds on existing BinLocatorActivity, batch features
  and RecordUploader.
- **Problem solved**: batch tools currently clutter the UI when scanning
  one item at a time.

## What
- Batch Mode checkbox on the main screen determines the workflow.
- Non‑batch mode shows a 5×N grid of bin buttons (9‑65 & F1‑F4) after OCR.
- Batch mode keeps Add Item, Show Items and the existing Send Record flow.
- Bin selection in default mode immediately posts the record then hides
the menu.

### Success Criteria
- [ ] Batch checkbox launches Bin Locator with batch flag.
- [ ] Default mode displays the full screen bin menu when roll and
      customer are present.
- [ ] Choosing a bin sends the record and resets the view.
- [ ] Batch mode shows Add Item and Show Items buttons only.
- [ ] Gradle lint and tests pass.

## All Needed Context

### Documentation & References (list all context needed to implement the feature)
```yaml
- url: https://developer.android.com/guide/topics/ui/controls/checkbox
  why: Adding and reading a CheckBox in MainActivity.
- url: https://developer.android.com/guide/topics/ui/layout/gridlayout
  why: Building a grid of buttons for the bin menu overlay.
- url: https://developer.android.com/topic/architecture
  why: Follow recommended separation of concerns.
- file: app/src/main/java/com/example/app/MainActivity.kt
  why: Pattern for passing intent extras (debug mode & batch flag).
- file: app/src/main/java/com/example/app/BinLocatorActivity.kt
  why: Existing capture flow, showBinMenu implementation and batch logic.
- file: app/src/main/res/layout/activity_main.xml
  why: Layout pattern for home screen widgets.
- file: app/src/main/res/layout/activity_bin_locator.xml
  why: Layout for action buttons and preview containers.
```

### Current Codebase tree (run `tree` in the root of the project) to get an overview of the codebase
```bash
./
├── AppFeatures.txt
├── INITIAL.md
├── PRPs/
│   ├── batch_binning.md
│   ├── debug_mode.md
│   ├── improved_ocr_pipeline.md
│   └── send_record.md
├── app/
│   ├── build.gradle
│   └── src
``` 

### Desired Codebase tree with files to be added and responsibility of file
```bash
app/src/main/res/layout/activity_main.xml            # Adds batchCheckBox
app/src/main/java/com/example/app/MainActivity.kt    # Passes batch flag
app/src/main/res/layout/activity_bin_locator.xml     # Overlay binMenuContainer
app/src/main/res/layout/dialog_bins.xml              # GridLayout with 5 columns
app/src/main/java/com/example/app/BinLocatorActivity.kt # Mode logic & menu
app/src/test/java/com/example/app/MainActivityUnitTest.kt  # Extra check test
app/src/androidTest/java/com/example/app/BinMenuUiTest.kt   # Overlay behaviour
```

### Known Gotchas of our codebase & Library Quirks
```kotlin
// Network operations must not run on the main thread.
// Robolectric tests are ignored in CI; annotate with @Ignore where needed.
// Intent extras default to false if missing; handle explicitly.
```

## Implementation Blueprint

### Data models and structure
No new data models required. Use a boolean intent extra `batch` and show
or hide existing batch-related views accordingly.

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  MODIFY app/src/main/res/layout/activity_main.xml:
    - Add CheckBox id=batchCheckBox above debugCheckBox.

Task 2:
  MODIFY app/src/main/java/com/example/app/MainActivity.kt:
    - Read batchCheckBox.isChecked and pass intent extra "batch" when starting
      BinLocatorActivity.

Task 3:
  MODIFY app/src/main/res/layout/activity_bin_locator.xml:
    - Wrap previewContainer with FrameLayout containing new ViewGroup
      id=binMenuContainer initially GONE.
    - Hide addItemButton and showBatchButton unless batchMode is true.

Task 4:
  CREATE app/src/main/res/layout/dialog_bins.xml:
    - GridLayout with 5 columns of Buttons labeled 9-65 and F1-F4.
    - Takes entire screen except padding bottom so capture button area
      remains clickable for dismissal.

Task 5:
  MODIFY app/src/main/java/com/example/app/BinLocatorActivity.kt:
    - Retrieve batchMode from intent; default false.
    - In onCreate show/hide batch UI based on batchMode.
    - Add function showBinOverlay() inflating dialog_bins.xml and attaching
      to binMenuContainer.
    - Call showBinOverlay() from showResult() when batchMode is false and both
      roll and customer lines detected.
    - Each bin button calls applyBin(value) then sendRecord() and hides overlay.
    - Tapping outside overlay removes it without sending.

Task 6:
  UPDATE app/src/main/java/com/example/app/BinLocatorActivity.kt sendRecord():
    - After successful post clear ocrTextView and hide actionButtons as today.

Task 7:
  CREATE app/src/test/java/com/example/app/MainActivityUnitTest.kt:
    - Verify intent extra "batch" matches checkbox state.

Task 8:
  CREATE app/src/androidTest/java/com/example/app/BinMenuUiTest.kt:
    - Trigger showResult with roll/customer and assert bin menu appears in
      non-batch mode and Add Item button hidden.

Task 9:
  UPDATE README.md and AppFeatures.txt documenting new batch toggle and
    automatic bin menu.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 5 snippet
private fun showBinOverlay() {
    val view = layoutInflater.inflate(R.layout.dialog_bins, binMenuContainer, false)
    view.findViewById<View>(R.id.overlayBackground).setOnClickListener {
        binMenuContainer.removeAllViews()
        binMenuContainer.visibility = View.GONE
    }
    view.findViewById<Button>(R.id.bin9).setOnClickListener {
        applyBin("9")
        sendRecord()
        binMenuContainer.removeAllViews()
        binMenuContainer.visibility = View.GONE
    }
    binMenuContainer.addView(view)
    binMenuContainer.visibility = View.VISIBLE
}
```

### Integration Points
```yaml
CONFIG: none
DATABASE: uses existing insert.php via RecordUploader
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
- ❌ Don't leave the overlay visible after sending a record.
- ❌ Don't forget to respect the batch flag when setting visibility.

### PRP Confidence Score: 6/10
