name: "Bin Locator Bounding Box & Orientation"
description: |
  ## Purpose
  Enhance the Bin Locator feature by adding a visible cropping overlay and
  orientation indicator. This allows users to position text within the OCR
  region and hold the device correctly.

  ## Core Principles
  1. **Context is King**: Provide docs on overlay drawing and ML Kit usage.
  2. **Validation Loops**: Gradle commands for lint, unit, and UI tests.
  3. **Information Dense**: Reference current activity and layout files.
  4. **Progressive Success**: Start with static overlay, then integrate cropping.
  5. **Global rules**: follow repository guidelines in CODEX.md.

---

## Goal
Add a 34:15 aspect ratio bounding box overlay covering 85% of the screen and
show green "TOP" text to indicate orientation. Crop the captured image to this
box before passing it to ML Kit.

## Why
- **Business value**: Improves accuracy by guiding users to frame text correctly.
- **Integration**: Extends existing BinLocatorActivity with minimal UI changes.
- **Problem solved**: Eliminates user confusion about device orientation and
  ensures consistent input size for OCR.

## What
- Fixed landscape-style bounding box overlay (34:15 ratio) regardless of device
  orientation.
 - Overlay occupies ~85% of the screen's width/height while preserving ratio.
- Green "TOP" label at the top edge of the overlay.
- Crop bitmap to overlay bounds before creating `InputImage` for ML Kit.

### Success Criteria
- [ ] Overlay renders in both portrait and landscape modes with correct aspect
      ratio and "TOP" label.
- [ ] Captured image cropped to overlay region and processed by ML Kit.
- [ ] Gradle lint, unit tests, and instrumentation tests pass.

## All Needed Context

### Documentation & References
```yaml
- url: https://developer.android.com/training/camerax
  why: guidance on CameraX preview and overlay usage.
- url: https://developer.android.com/ml-kit/vision/text-recognition
  why: reference for creating `InputImage` from Bitmap.
- file: app/src/main/java/com/example/app/BinLocatorActivity.kt
  why: existing capture flow and rotation logic.
- file: app/src/main/res/layout/activity_bin_locator.xml
  why: layout to modify with overlay and label.
```

### Current Codebase tree
```bash
$(tree -L 2 | head -n 30)
```

### Desired Codebase tree with files to be added and responsibility of file
```bash
.
├── app/src/main/java/com/example/app/BoundingBoxOverlay.kt     # Custom View drawing 34:15 box and TOP text
├── app/src/main/res/layout/activity_bin_locator.xml            # Updated layout with overlay
├── app/src/test/java/com/example/app/BoundingBoxUtilTest.kt     # Unit test for crop calculation
```

### Known Gotchas of our codebase & Library Quirks
```kotlin
// Always close ImageProxy after processing to avoid leaks.
// Ensure cropping calculations account for device rotation.
```

## Implementation Blueprint

### Data models and structure
No new persistent models. `BoundingBoxOverlay` computes crop rect in view
coordinates and exposes helper to map to bitmap coordinates.

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  CREATE app/src/main/java/com/example/app/BoundingBoxOverlay.kt:
    - Custom view extending View.
    - Draw a static rectangular outline with 34:15 aspect ratio occupying 85% of
      min(width, height).
    - Render green "TOP" text centered on the top edge.
    - Provide `getCropRect()` returning the rect in view coordinates.

Task 2:
  MODIFY activity_bin_locator.xml:
    - Wrap PreviewView and overlay in FrameLayout so overlay sits on top.
    - Add the new BoundingBoxOverlay view.

Task 3:
  MODIFY BinLocatorActivity.kt:
    - Obtain crop rect from overlay.
    - After rotating bitmap, crop to rect scaled to bitmap size before OCR.

Task 4:
  CREATE BoundingBoxUtilTest.kt:
    - Unit tests verifying aspect ratio calculations and crop rect scaling.

Task 5:
  UPDATE AppFeatures.txt and README.md with new capability.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 1 pseudocode
class BoundingBoxOverlay(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    override fun onDraw(canvas: Canvas) {
        val boxSize = 0.7f * min(width, height)
        val boxWidth = boxSize
        val boxHeight = boxSize * 15f / 34f
        val left = (width - boxWidth) / 2
        val top = (height - boxHeight) / 2
        val rect = RectF(left, top, left + boxWidth, top + boxHeight)
        canvas.drawRect(rect, paint)
        canvas.drawText("TOP", rect.centerX(), rect.top - 10, textPaint)
    }
}
```

### Integration Points
```yaml
GRADLE:
  - No new dependencies.
ROUTES:
  - MainActivity unchanged; continues to launch BinLocatorActivity.
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
- [ ] Overlay shows bounding box and TOP label in app
- [ ] Cropped image processed by ML Kit and result displayed

## Anti-Patterns to Avoid
- ❌ Do not rely on auto-rotation; cropping must use overlay rect.
- ❌ Do not create overly complex custom views; keep under 500 lines.

### PRP Confidence Score: 7/10
