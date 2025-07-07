# Basic Android App

A minimal Android project using Kotlin and Gradle. The project builds an Empty Activity showing "Hello World" and includes example unit and instrumentation tests.

## Setup

After cloning the repository, decode the Gradle wrapper JAR:

```bash
python scripts/decode_gradle_wrapper.py
```

This step recreates `gradle/wrapper/gradle-wrapper.jar`, which is required for running Gradle tasks.

## Build and Test

Run the following commands from the project root:

```bash
./gradlew lint
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

Instrumentation tests require an Android emulator or device configured with the Android SDK.

## Requirements

- JDK 17 or newer
- Android SDK and platform tools
- Android Studio (recommended) for emulator and IDE support
