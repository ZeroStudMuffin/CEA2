name: "Checkout Items Module"
description: |
  ## Purpose
  Add a new screen for checking items out of the warehouse database.
  It mirrors the Bin Locator layout but omits release/barcode tools
  and replaces the **Send Record** flow with a confirmation driven
  **Checkout** action posting to `checkout.php`.
  The goal is minimal duplication by reusing existing OCR and batch
  code paths.

  ## Core Principles
  1. **Context is King**: reference current activity and uploader patterns.
  2. **Validation Loops**: Gradle lint plus unit and instrumentation tests.
  3. **Information Dense**: follow Kotlin style already in the repo.
  4. **Progressive Success**: build uploader, create activity, then wire UI.
  5. **Global rules**: follow repository guidelines in CODEX.md.

---

## Goal
Provide a "Checkout Items" button on the home screen that launches a
`CheckoutActivity`. Users capture a label as usual and queue multiple
roll/customer pairs. Pressing **Checkout** asks for confirmation then
POSTs each queued item with the user's PIN to `checkout.php`. Success
marks them checked out in the database and clears the list.

## Why
- **Business value**: tracks pallets leaving the warehouse.
- **Integration**: builds on the OCR pipeline and batch item logic.
- **Problem solved**: manual checkout is error prone and slow.

## What
- New activity with same camera preview and OCR as BinLocatorActivity.
- Batch mode only: Add Item and Show Items buttons visible; others hidden.
- Confirmation dialog before sending checkout request.
- POST fields: `roll_num`, `customer`, `pin` (4‑digit from login).
- Reuse parsing and batch storage to avoid code bloat.

### Success Criteria
- [ ] Checkout button visible once at least one item is queued.
- [ ] Confirmation dialog lists item count and asks to proceed.
- [ ] POST request returns HTTP 200 and JSON `{"status":"success"}`.
- [ ] On success the queued list clears and Snackbar confirms checkout.
- [ ] Gradle lint and tests pass.

## All Needed Context

### Documentation & References (list all context needed to implement the feature)
```yaml
- url: https://developer.android.com/topic/architecture
  why: Follow recommended activity and data separation patterns.
- url: https://developer.android.com/reference/androidx/appcompat/app/AlertDialog
  why: Building confirmation popups.
- file: app/src/main/java/com/example/app/BinLocatorActivity.kt
  why: Base camera + OCR flow and batch item handling.
- file: app/src/main/java/com/example/app/RecordUploader.kt
  why: Pattern for background HTTP POST.
- file: app/src/main/res/layout/activity_bin_locator.xml
  why: Layout example for preview and action buttons.
- file: examples/checkout.php
  why: Server requirements for checkout update.
```

### Current Codebase tree (run `tree` in the root of the project) to get an overview of the codebase
```bash
.
├── INITIAL.md
├── PRPs/
│   ├── send_record.md
│   └── templates/
├── app/
│   ├── build.gradle
│   └── src
```

### Desired Codebase tree with files to be added and responsibility of file
```bash
app/src/main/res/layout/activity_checkout.xml          # Layout mirroring bin locator with Checkout button
app/src/main/java/com/example/app/CheckoutActivity.kt   # Camera + OCR flow for checkout
app/src/main/java/com/example/app/CheckoutUploader.kt   # POST to checkout.php
app/src/test/java/com/example/app/CheckoutUploaderTest.kt  # Unit tests for uploader
app/src/androidTest/java/com/example/app/CheckoutUiTest.kt  # UI behaviour and confirmation dialog
```

### Known Gotchas of our codebase & Library Quirks
```kotlin
// Network operations must not run on the main thread.
// Robolectric tests are ignored in CI; annotate with @Ignore as done in RecordUploaderTest.
// Intent extras default to false/empty if missing; pass the PIN explicitly.
```

## Implementation Blueprint

### Data models and structure
Use existing `BatchRecord` to queue items. Create a `CheckoutUploader` object
modeled after `RecordUploader` but posting to `checkout.php` and accepting the
user's PIN.

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  MODIFY app/src/main/res/layout/activity_main.xml:
    - Add Button id=checkoutButton below binLocatorButton.

Task 2:
  MODIFY app/src/main/java/com/example/app/MainActivity.kt:
    - Store entered PIN after validation.
    - Launch CheckoutActivity with extras "pin" and "batch" always true.

Task 3:
  CREATE app/src/main/res/layout/activity_checkout.xml:
    - Copy from activity_bin_locator.xml.
    - Remove getReleaseButton, setBinButton and debug controls.
    - Keep addItemButton, showBatchButton and a new checkoutButton.

Task 4:
  CREATE app/src/main/java/com/example/app/CheckoutUploader.kt:
    - Function checkoutItems(items: List<BatchRecord>, pin: String, onComplete: (Boolean,String?) -> Unit).
    - POST arrays roll_num[], customer[] and pin to checkout.php similar to RecordUploader.

Task 5:
  CREATE app/src/main/java/com/example/app/CheckoutActivity.kt:
    - Reuse BinLocatorActivity logic for camera setup and OCR parsing.
    - Always operate in batch mode; queue items with onAddItem().
    - Enable checkoutButton when list not empty. On click show AlertDialog
      summarizing count; on confirm call CheckoutUploader and clear list on success.

Task 6:
  CREATE app/src/test/java/com/example/app/CheckoutUploaderTest.kt:
    - Mock HttpURLConnection like RecordUploaderTest to verify payload and success/failure cases.

Task 7:
  CREATE app/src/androidTest/java/com/example/app/CheckoutUiTest.kt:
    - Launch CheckoutActivity, invoke showResult via reflection and verify
      checkoutButton enabled and dialog shows.

Task 8:
  UPDATE AppFeatures.txt and README.md documenting the checkout workflow.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 4 snippet
object CheckoutUploader {
    private val executor = Executors.newSingleThreadExecutor()

    fun checkoutItems(items: List<BatchRecord>, pin: String, onComplete: (Boolean, String?) -> Unit) {
        executor.execute {
            try {
                val url = URL("https://unitedexpresstrucking.com/checkout.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                val body = buildString {
                    items.forEachIndexed { i, item ->
                        append("roll_num[]=${item.roll}&customer[]=${item.customer}&")
                    }
                    append("pin=$pin")
                }
                conn.outputStream.use { it.write(body.toByteArray()) }
                val success = conn.responseCode == 200
                val msg = conn.inputStream.bufferedReader().readText()
                onComplete(success, msg)
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }
}
```

### Integration Points
```yaml
CONFIG: none
DATABASE: uses checkout.php to update pallet_info table
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
- [ ] Confirmation dialog shows correct count
- [ ] Items cleared only on success

---

## Anti-Patterns to Avoid
- ❌ Don't duplicate BinLocator logic; extract shared pieces if needed.
- ❌ Don't run network code on the UI thread.
- ❌ Don't ignore failing tests.

### PRP Confidence Score: 7/10
