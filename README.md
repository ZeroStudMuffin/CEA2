# Basic Android App

A minimal Android project using Kotlin and Gradle. The project builds an Empty Activity showing "Hello World" and includes example unit and instrumentation tests.

## Setup
AGENTS MUST DO ALL SETUP STEPS AT THE START OF THE CONVERSATION! DO NOT SKIP THESE STEPS OR TESTING WILL FAIL!

After cloning the repository, decode the Gradle wrapper JAR:

```bash
python scripts/decode_gradle_wrapper.py
```

This step recreates `gradle/wrapper/gradle-wrapper.jar`, which is required for running Gradle tasks.

Fetch the Git LFS files to obtain the bundled Android command line tools (`commandlinetools-linux-13114758_latest.zip`).
If the repository was cloned without a remote configured, add one before
pulling:

```bash
git remote add origin https://github.com/ZeroStudMuffin/CEA2.git
git lfs pull
```

Extract the command line tools so the `sdkmanager` tool is available before
running the installer:

```bash
unzip commandlinetools-linux-13114758_latest.zip -d android-tools
export PATH="$PWD/android-tools/cmdline-tools/bin:$PATH"
```

Next, install the Android SDK:

```bash
chmod +x scripts/install_android_sdk.sh  # ensures the script can run if cloned without executable permissions
./scripts/install_android_sdk.sh
```

The script uses the `commandlinetools-linux-13114758_latest.zip` archive from
Git LFS to install the SDK without requiring network access. If the archive is
missing it will download the tools from Google instead.

Set the `ANDROID_HOME` environment variable to the SDK path (default is `~/android-sdk`) and add `$ANDROID_HOME/platform-tools` to your `PATH`.  Gradle looks for the SDK via `local.properties`, so create this file in the project root containing:

```
sdk.dir=/path/to/android-sdk
```

Instrumentation tests require an emulator package, for example:

```bash
sdkmanager "system-images;android-34;google_apis;x86_64" "emulator"
```

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

 - Requires a 4 digit PIN on launch. Accepted PINs are fetched from a
   Google Sheet so only authorized users can proceed.
 - Camera-based **Bin Locator** with a bounding box overlay guiding where to place
  text for OCR. The box now covers about **85%** of the screen for easier framing.
- Captured images are cropped to this box and processed with ML Kit text
  recognition.
- Camera preview supports pinch-to-zoom with a slider and a 1x reset button for
  finer control when capturing text.
- A rotate button switches the app between portrait and landscape modes, ensuring captured images match the screen orientation.
 - Recognised text is shown in a TextView with a **Get Release** button that
   scans barcodes using ML Kit. The **Set Bin** button now opens a pop-up menu
  listing bins 19-65 plus "Floor BR" and "Floor BL". Selecting a value sets
  `BIN=<bin>` on the roll number line, replacing any previous value. Barcode
  scanning for bins is disabled.
- Each OCR line's bounding box height is printed to logcat alongside the text.
- OCR results are cleaned with `OcrParser` before barcode scanning.
- The parser now outputs only a roll number and customer name, displayed on two
  lines in the Bin Locator screen. Any prefix before the first space in the
  roll number is removed so users see only the numeric portion.
- Once roll, customer and bin are present a **Send Record** button appears.
  Tapping uploads the data to the server and clears the text view. If the server
  returns an error, the provided message is shown instead of a generic failure.
