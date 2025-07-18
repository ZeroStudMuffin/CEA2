# Basic Android App

A minimal Android project using Kotlin and Gradle. The project builds an Empty Activity showing "Hello World" and includes example unit and instrumentation tests.

## Build and Test

Run the following commands from the project root:

```bash
./gradlew lint
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

Instrumentation tests require an Android emulator or device configured with the Android SDK.
This app relies on Material Components. A custom theme extending `Theme.MaterialComponents.DayNight.NoActionBar` is defined in `app/src/main/res/values/styles.xml` and referenced from the manifest.

## Requirements

- JDK 17 or newer
- Android SDK and platform tools
- Android Studio (recommended) for emulator and IDE support

## Features

 - Camera-based **Bin Locator** with a bounding box overlay guiding where to place
  text for OCR. The box spans about **60%** of the screen width and uses a configurable target aspect ratio (default 8.5:3.625) so the crop matches the expected label shape.
- Captured images are cropped to this box and processed with ML Kit text
  recognition.
- Camera preview supports pinch-to-zoom with a slider. The preview starts at a
  zoom level of 40% for easier text alignment.
- The screen orientation is locked to portrait; rotating the device has no effect.
 - Recognised text is shown in a TextView with **Get Release** and **Set Bin**
   buttons when batch mode is enabled. The Set Bin option opens a pop-up menu
  listing bins 19-65 plus "Floor BR" and "Floor BL". Selecting a value sets
  `BIN=<bin>` on the roll number line, replacing any previous value. Barcode
  scanning for bins is disabled.
- Each OCR line's bounding box height is printed to logcat alongside the text.
- OCR results are cleaned with `OcrParser` before barcode scanning.
- Captured labels are perspective corrected using OpenCV and converted to grayscale prior to OCR for better accuracy. When debugging, the **Show Crop** button saves this grayscale warped image before displaying it so you can inspect exactly what ML Kit saw.
- The **Show Log** button in debug mode displays any debug messages captured during processing.
 - The parser now outputs only a roll number and customer name, displayed on two
  lines in the Bin Locator screen. Any prefix before the final underscore in the
  roll number is removed so users see only the numeric portion.
- Once roll, customer and bin are present a **Send Record** button appears when
  batch mode is enabled. In default mode a full-screen bin menu pops up and
  selecting a value uploads immediately. Requests are POSTed with the user's
  PIN included as `last_user` and an `X-API-Key` header for authentication. If
  the server returns an error, the provided message is shown instead of a
  generic failure.
 - A **Debug mode** checkbox on the main screen launches Bin Locator with sending
  disabled. Additional **Show OCR**, **Show Crop** and **Show Log** buttons reveal raw text
  with bounding box heights, an exact crop preview showing the warped image
  passed to ML Kit, and a dialog with collected debug logs for troubleshooting.
- A **Tune Pipeline** button in debug mode opens sliders and fields for
  adjusting OCR preprocessing values during the current session. Each
  parameter has a checkbox to enable or disable that step. The `min_area`
  and `ratio_tolerance` sliders now use a 0–1 range shown as a percentage.
- Entering PIN **8789** unlocks a Developer screen with the debug toggle and a
  **Preprocess Debug** option. This screen centralises tools only meant for
  developers.
- **Preprocess Debug** captures an image and immediately shows the warped
  grayscale result without running ML Kit, letting developers inspect tuning
  effects.
- **Live Edge Preview** continuously processes preview frames showing the
  Canny edge output so tuning changes take effect in real time.
- A **Batch mode** checkbox on the main screen controls whether captures queue
  multiple items or use a single-record flow. When unchecked a full-screen
  **Bins** menu appears once roll and customer are recognised; choosing a bin
  immediately uploads the record. The capture button and zoom slider disappear
  while this menu is visible. When checked an **Add Item** button saves each
  roll/customer pair and a **Show Items** dialog lists them so they can be sent
  together. An **Input Item** button lets users manually enter a roll and
  customer pair when OCR isn't used.
- On startup the app fetches a list of valid 4-digit PINs from a Google Sheet
  and prompts the user to enter one. The main screen remains disabled until a
  correct PIN is provided.
- A **Checkout Items** option on the main screen opens a camera screen similar
  to Bin Locator. Items can be queued and confirmed with a Checkout button which
  posts them with the user's PIN (sent as `last_user`) to `checkout.php`. All
  uploads now include an `X-API-Key` header for authentication.
  When debug mode is enabled this screen also offers a **Show Log** button for
  reviewing debug messages.
