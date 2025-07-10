name: "OCR Parsing Utility"
description: |
  ## Purpose
  Add a dedicated Kotlin parser to clean up OCR output for the Bin Locator feature. Lines shorter than 75% of the tallest line should be ignored. The parser also strips special characters (except _, -, %) and converts underscores to spaces. Text enclosed in brackets or quotes is removed. The logic will be documented for future agents.

  ## Core Principles
  1. **Context is King**: reference ML Kit text recognition docs and existing utility patterns.
  2. **Validation Loops**: provide Gradle commands for linting and tests.
  3. **Information Dense**: follow Kotlin utility and test patterns in the repo.
  4. **Progressive Success**: build parser, add tests, then integrate.
  5. **Global rules**: follow repository guidelines in CODEX.md.

---

## Goal
Provide an `OcrParser` utility that filters and cleans recognized text lines. Output is a list of sanitized strings used for downstream barcode matching.

## Why
- **Business value**: improves accuracy of release/bin extraction.
- **Integration**: plugs into existing `BinLocatorActivity` OCR flow.
- **Problem solved**: removes noisy results that currently clutter logs and reduce matching reliability.

## What
- Kotlin parser file with unit tests.
- `parser-explained.txt` documenting logic.
- Integration call in `BinLocatorActivity`.

### Success Criteria
- [ ] `OcrParser.parse()` returns cleaned lines according to rules.
- [ ] Unit tests cover normal, edge, and failure cases.
- [ ] README and AppFeatures mention new parser.
- [ ] `parser-explained.txt` created with step-by-step explanation.

## All Needed Context

### Documentation & References (list all context needed to implement the feature)
```yaml
- url: https://developers.google.com/ml-kit/vision/text-recognition/android
  why: Understanding Text.Line API and bounding box properties.
- url: https://developer.android.com/topic/architecture
  why: Follow recommended separation of concerns.
- file: app/src/main/java/com/example/app/BarcodeUtils.kt
  why: Shows current regex and utility style.
- file: app/src/main/java/com/example/app/BinLocatorActivity.kt
  why: Existing OCR result handling and logging.
```

### Current Codebase tree (run `tree` in the root of the project) to get an overview of the codebase
```bash
.
├── app/src/main/java/com/example/app/
│   ├── BarcodeUtils.kt
│   ├── BinLocatorActivity.kt
│   ├── BoundingBoxOverlay.kt
│   ├── ImageUtils.kt
│   ├── MainActivity.kt
│   └── ZoomUtils.kt
├── app/src/test/java/com/example/app/
│   ├── BarcodeUtilTest.kt
│   ├── BinLocatorUnitTest.kt
│   ├── BoundingBoxUtilTest.kt
│   ├── MainActivityUnitTest.kt
│   └── ZoomUtilTest.kt
```

### Desired Codebase tree with files to be added and responsibility of file
```bash
app/src/main/java/com/example/app/OcrParser.kt     # Parsing logic for OCR lines
app/src/test/java/com/example/app/OcrParserTest.kt  # Unit tests for parser
parser-explained.txt                               # Text explanation of parser steps
```

### Known Gotchas of our codebase & Library Quirks
```kotlin
// ML Kit Text.Line.boundingBox may be null; handle null height as 0.
// Unit tests using Robolectric may require @RunWith(RobolectricTestRunner::class).
```

## Implementation Blueprint

### Data models and structure
No persistent models; a singleton object `OcrParser` with a `parse(lines: List<Text.Line>): List<String>` function.

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  CREATE app/src/main/java/com/example/app/OcrParser.kt:
    - Object with `parse` function accepting `List<Text.Line>`.
    - Determine tallest line height; ignore lines < 75% of that height.
    - Replace '_' with space, strip all chars except letters, digits, space, '-', '%'.
    - Remove text in brackets [] or () and in single/double quotes.
    - Return cleaned text lines.

Task 2:
  CREATE app/src/test/java/com/example/app/OcrParserTest.kt:
    - Tests for typical input, all lines filtered, and special-char removal.

Task 3:
  MODIFY app/src/main/java/com/example/app/BinLocatorActivity.kt:
    - After OCR success, pass `block.lines` to `OcrParser.parse()` and log/ display result.

Task 4:
  CREATE parser-explained.txt in project root:
    - Describe parser operations in order with brief reasoning.

Task 5:
  UPDATE AppFeatures.txt and README.md documenting parser utility.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 1 pseudocode
object OcrParser {
    fun parse(lines: List<Text.Line>): List<String> {
        val tallest = lines.maxOfOrNull { it.boundingBox?.height() ?: 0 } ?: 0
        val threshold = tallest * 0.75
        return lines.filter { (it.boundingBox?.height() ?: 0) >= threshold }
            .map { line ->
                var text = line.text.replace('_', ' ')
                text = text.replace(Regex("[\"'].*?[\"']"), "")
                text = text.replace(Regex("\[[^\]]*\]|\([^)]*\)"), "")
                text.replace(Regex("[^A-Za-z0-9 %\- ]"), "").trim()
            }
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

### Level 3: Instrumentation Test (existing ones)
```bash
./gradlew connectedDebugAndroidTest
```

## Final validation Checklist
- [ ] All tests pass: `./gradlew testDebugUnitTest connectedDebugAndroidTest`
- [ ] No linting errors: `./gradlew lint`
- [ ] Parser logic explained in `parser-explained.txt`
- [ ] README and AppFeatures updated

---

## Anti-Patterns to Avoid
- ❌ Don't mutate ML Kit objects directly.
- ❌ Don't ignore null bounding boxes.
- ❌ Don't skip unit tests for edge cases.

### PRP Confidence Score: 8/10
