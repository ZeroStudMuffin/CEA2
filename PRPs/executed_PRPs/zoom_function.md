name: "Camera Zoom Feature"
description: |
  ## Purpose
  Add pinch-to-zoom functionality with a slider control and a 1x reset button to the Bin Locator camera preview. This improves usability when capturing text at different distances.

  ## Core Principles
  1. **Context is King**: Provide docs on CameraX zoom and PreviewView controller.
  2. **Validation Loops**: Gradle commands for lint and tests.
  3. **Information Dense**: Reference current activity and layout files.
  4. **Progressive Success**: Start with basic zoom control then refine UI.
  5. **Global rules**: follow repository guidelines in CODEX.md.

---

## Goal
Implement pinch and slider-based zoom in BinLocatorActivity with a 1x reset button. Zoom level should never be less than 1x.

## Why
- **Business value**: Users can capture text at varying distances without losing clarity.
- **Integration**: Extends existing Bin Locator Activity and layout.
- **Problem solved**: Provides fine-grained control of camera zoom for more accurate OCR.

## What
- Pinch gesture zoom on PreviewView.
- Slider shows current zoom and allows direct adjustment.
- 1x button resets zoom to default.
- Prevent zoom ratios below 1x.

### Success Criteria
- [ ] Pinch gesture changes zoom smoothly.
- [ ] Slider position updates when pinch zooming.
- [ ] Slider changes adjust zoom via CameraController.
- [ ] Tapping 1x button resets to min zoom ratio (1x).
- [ ] Gradle lint, unit tests, and instrumentation tests pass.

## All Needed Context

### Documentation & References
```yaml
- url: https://developer.android.com/training/camerax/configuration#camera-output
  why: using CameraControl#setZoomRatio and setLinearZoom with ZoomState.
- url: https://developer.android.com/reference/androidx/camera/view/PreviewView#setController(androidx.camera.view.CameraController)
  why: PreviewView + CameraController enables pinch-to-zoom.
- file: app/src/main/java/com/example/app/BinLocatorActivity.kt
  why: existing camera setup and capture flow.
- file: app/src/main/res/layout/activity_bin_locator.xml
  why: layout to extend with slider and reset button.
```

### Current Codebase tree
```bash
$(tree -L 2 | head -n 20)
```

### Desired Codebase tree with files to be added and responsibility of file
```bash
.
├── app/src/main/java/com/example/app/BinLocatorActivity.kt       # Add zoom logic
├── app/src/main/res/layout/activity_bin_locator.xml              # Add Slider & 1x button
├── app/src/test/java/com/example/app/ZoomUtilTest.kt             # Unit tests for clamping
├── app/src/androidTest/java/com/example/app/ZoomUiTest.kt        # Instrumentation test for slider presence
```

### Known Gotchas of our codebase & Library Quirks
```kotlin
// Always unbind camera use cases before binding new ones.
// setLinearZoom() expects value 0-1; clamp ratio to >= 1f using setZoomRatio.
```

## Implementation Blueprint

### Data models and structure
No new persistent models. BinLocatorActivity holds a CameraController and observes zoomState to update the slider.

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  MODIFY app/src/main/res/layout/activity_bin_locator.xml:
    - Add Material Slider below preview within the constraint layout.
    - Add a small "1x" Button to the left of the slider.

Task 2:
  MODIFY app/src/main/java/com/example/app/BinLocatorActivity.kt:
    - Replace manual camera binding with CameraController set on PreviewView.
    - Observe cameraController.zoomState to update slider value and text.
    - On slider change call cameraController.setLinearZoom().
    - On 1x button click call cameraController.setZoomRatio(1f).
    - Clamp any incoming zoom ratio to >= 1f.

Task 3:
  CREATE app/src/test/java/com/example/app/ZoomUtilTest.kt:
    - Unit test verifying clampZoomRatio() returns at least 1f.

Task 4:
  CREATE app/src/androidTest/java/com/example/app/ZoomUiTest.kt:
    - Launch BinLocatorActivity and check slider and 1x button are displayed.

Task 5:
  UPDATE AppFeatures.txt and README.md with new zoom capability.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 2 pseudocode
class BinLocatorActivity : AppCompatActivity() {
    private lateinit var controller: CameraController
    private lateinit var slider: Slider
    private lateinit var resetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        // existing setup ...
        controller = LifecycleCameraController(this)
        controller.bindToLifecycle(this)
        previewView.controller = controller // enables pinch-to-zoom
        slider.addOnChangeListener { _, value, _ ->
            controller.setLinearZoom(value)
        }
        resetButton.setOnClickListener {
            controller.setZoomRatio(1f)
        }
        controller.zoomState.observe(this) { state ->
            val zoomRatio = max(1f, state.zoomRatio)
            slider.value = state.linearZoom
        }
    }
}
```

### Integration Points
```yaml
GRADLE:
  - No additional dependencies (Material library already included).
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
- [ ] Zoom controls visible and functional in app
- [ ] Documentation updated with new feature

---

## Anti-Patterns to Avoid
- ❌ Do not allow zoom ratio below 1x.
- ❌ Do not bypass CameraController when pinch-to-zoom is required.
- ❌ Do not create overly complex UI; keep files under 500 lines.

### PRP Confidence Score: 7/10
