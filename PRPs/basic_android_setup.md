name: "Android App Base Setup"
description: |
  ## Purpose
  Create a minimal Android application skeleton that can be built and run in Android Studio. The project should include
  scaffolding for unit tests and instrumentation tests so Codex can execute them automatically. No app features yet, only
  baseline project setup, build configuration, and basic CI commands.

  ## Core Principles
  1. **Context is King**: include essential docs and examples for Android project structure.
  2. **Validation Loops**: provide gradle commands for linting, building and testing the app.
  3. **Information Dense**: show references to example projects from the repository specification.
  4. **Progressive Success**: start from the Empty Activity template then integrate testing framework.
  5. **Global rules**: follow repository guidelines in CODEX.md.

---

## Goal
Build a Kotlin Android project using Gradle with an Empty Activity. The project must compile in Android Studio,
run on the emulator, and include initial unit and instrumentation tests that can be executed by Codex.

## Why
- **Business value**: establishing the base project allows future features to be added easily.
- **Integration**: the structure will mirror standard Android templates so new modules follow common patterns.
- **Problem solved**: ensures Codex can build, test, and debug the app from the onset.

## What
- Android Studio compatible gradle project using Kotlin.
- Initial MainActivity showing a simple "Hello World".
- Unit test and instrumentation test sample verifying MainActivity title.
- README instructions for building and testing with gradle.

### Success Criteria
- [ ] Project builds without errors: `./gradlew assembleDebug`
- [ ] Unit tests pass: `./gradlew testDebugUnitTest`
- [ ] Instrumentation tests pass on emulator: `./gradlew connectedDebugAndroidTest`
- [ ] App launches on Android Studio emulator showing "Hello World".

## All Needed Context

### Documentation & References
```yaml
- url: https://developer.android.com/topic/architecture
  why: guidance on structuring the app using recommended patterns.
- url: https://github.com/android/nowinandroid?tab=readme-ov-file
  why: reference for a simple Android app repository layout.
- url: https://github.com/android/nowinandroid/blob/main/docs/ArchitectureLearningJourney.md
  why: official Google sample discussing architecture decisions.
- url: https://developer.android.com/training/basics/firstapp
  why: step-by-step instructions to create the first activity and configure Gradle.
- url: https://developer.android.com/training/testing
  why: overview of unit and instrumentation testing in Android.
```

### Current Codebase tree
```bash
$(tree -L 2)
```

### Desired Codebase tree
```bash
.
├── app
│   ├── src/main/java/com/example/app/MainActivity.kt
│   ├── src/androidTest/java/com/example/app/MainActivityTest.kt
│   ├── src/test/java/com/example/app/MainActivityUnitTest.kt
│   └── src/main/res/
├── build.gradle
├── settings.gradle
└── README.md
```

### Known Gotchas of our codebase & Library Quirks
```kotlin
// Gradle plugin versions must match Android Studio's recommended versions.
// Instrumentation tests require an emulator or device with matching API level.
// Use AndroidX Test libraries; avoid deprecated support libraries.
```

## Implementation Blueprint

### Data models and structure
No data models yet. MainActivity displays a static layout. Tests will use Espresso for UI verification and JUnit for unit tests.

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
  CREATE project structure using Android Studio Empty Activity template.
  - Ensure gradle wrapper is included (`gradlew`, `gradlew.bat`).
  - Configure `build.gradle` with Kotlin plugin and AndroidX dependencies.

Task 2:
  CREATE app/src/main/java/com/example/app/MainActivity.kt
  - Use Kotlin Activity derived from `AppCompatActivity`.
  - Inflate `activity_main.xml` layout containing a centered TextView with "Hello World".

Task 3:
  CREATE unit tests under `app/src/test/...` using JUnit4.
  - Example test verifies package name and simple logic.

Task 4:
  CREATE instrumentation tests under `app/src/androidTest/...` using Espresso.
  - Test `MainActivity` launches and displays "Hello World".

Task 5:
  CREATE README.md with commands for building and running tests.
  - Document gradle tasks used in validation gates.
```

### Per task pseudocode as needed added to each task
```kotlin
// Task 2 pseudocode
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
```

### Integration Points
```yaml
GRADLE:
  - build.gradle with Kotlin and Android Gradle Plugin.
  - settings.gradle specifying the app module.
TEST LIBRARIES:
  - JUnit4 for unit tests.
  - AndroidX Test (Espresso, Core) for instrumentation.
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
- [ ] Manual app launch successful: run on emulator via Android Studio
- [ ] README updated with build instructions

## Anti-Patterns to Avoid
- ❌ Do not add extra features beyond Hello World.
- ❌ Do not use deprecated Android support libraries.
- ❌ Do not ignore failing tests or lint warnings.


### PRP Confidence Score: 8/10
