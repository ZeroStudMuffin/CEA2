name: "Roll and Customer Extraction"
description: |
  ## Purpose
  Extend `OcrParser` so it returns only the roll number and customer name.
  The cleaned OCR lines from the existing logic will be further processed using
  simple heuristics. The result replaces the full text shown in
  `BinLocatorActivity`.

  ## Core Principles
  1. **Context is King**: follow Kotlin patterns in existing utils.
  2. **Validation Loops**: Gradle lint and unit tests.
  3. **Information Dense**: use ML Kit Text APIs and repo examples.
  4. **Progressive Success**: implement parser, add tests, integrate.
  5. **Global rules**: see CODEX.md.

---

## Goal
Output only `Roll#:<num>` and `Cust:<name>` after OCR.

## Why
- **Business value**: simplifies screen output for users.
- **Integration**: builds on existing `OcrParser` and activity flow.
- **Problem solved**: full OCR text currently clutters the view.

## What
- Modify `OcrParser.parse()` to detect roll and customer lines.
- Show the final two-line result in `BinLocatorActivity`.

### Success Criteria
- [ ] `parse()` returns exactly two lines.
- [ ] Unit tests cover known-word and numeric cases.
- [ ] README and AppFeatures updated.

## All Needed Context

### Documentation & References
```yaml
- url: https://developer.android.com/topic/architecture
  why: Follow recommended separation of concerns.
- file: app/src/main/java/com/example/app/OcrParser.kt
  why: Existing cleaning logic to extend.
- file: app/src/main/java/com/example/app/BinLocatorActivity.kt
  why: Shows how parser result is displayed.
- file: app/src/test/java/com/example/app/OcrParserTest.kt
  why: Testing pattern for Kotlin utilities.
```

### Current Codebase tree
```bash
app/src/main/java/com/example/app
├── BarcodeUtils.kt
├── BinLocatorActivity.kt
├── BoundingBoxOverlay.kt
├── ImageUtils.kt
├── MainActivity.kt
├── OcrParser.kt
└── ZoomUtils.kt

app/src/test/java/com/example/app
├── BarcodeUtilTest.kt
├── BinLocatorUnitTest.kt
├── BoundingBoxUtilTest.kt
├── MainActivityUnitTest.kt
├── OcrParserTest.kt
└── ZoomUtilTest.kt
```

### Desired Codebase tree
```bash
app/src/main/java/com/example/app/OcrParser.kt       # Updated logic
app/src/test/java/com/example/app/OcrParserTest.kt   # New tests for extraction
```

### Known Gotchas
```kotlin
// ML Kit bounding boxes may be null; treat height 0 accordingly.
// Robolectric tests are ignored in CI; use @Ignore annotations.
```

## Implementation Blueprint

### Data models and structure
Use the existing `OcrParser` singleton. Add constants for
`knownCustomerWords` and a helper to count digits in a string.

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  MODIFY app/src/main/java/com/example/app/OcrParser.kt:
    - After cleaning logic, compute `cleanLines`.
    - If any line contains a known word, set that as customer name.
      Then choose the longest remaining line as roll.
    - Else choose line with most digits as roll, and next longest line as customer.
    - Return listOf("Roll#:${roll}", "Cust:${customer}").

Task 2:
  MODIFY app/src/test/java/com/example/app/OcrParserTest.kt:
    - Add tests for both rule paths and failure case.

Task 3:
  MODIFY app/src/main/java/com/example/app/BinLocatorActivity.kt:
    - Display returned lines directly instead of joinToString.

Task 4:
  UPDATE parser-explained.txt with new steps.

Task 5:
  UPDATE README.md and AppFeatures.txt describing final OCR output.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 1 pseudocode
val cleanLines = existingCleaning(lines)
val customer = cleanLines.firstOrNull { it.containsKnownWord() }
val roll = if (customer != null)
    cleanLines.filterNot { it == customer }.maxByOrNull { it.length }
  else
    cleanLines.maxByOrNull { countDigits(it) }
val name = customer ?: cleanLines.filterNot { it == roll }.maxByOrNull { it.length }
return listOf("Roll#:${roll ?: ""}", "Cust:${name ?: ""}")
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

### Level 3: Instrumentation Test (existing ones)
```bash
./gradlew connectedDebugAndroidTest
```

## Final validation Checklist
- [ ] All tests pass: `./gradlew testDebugUnitTest connectedDebugAndroidTest`
- [ ] No linting errors: `./gradlew lint`
- [ ] README and AppFeatures updated

---

## Anti-Patterns to Avoid
- ❌ Don't mutate ML Kit objects.
- ❌ Don't skip unit tests for edge cases.

### PRP Confidence Score: 8/10
