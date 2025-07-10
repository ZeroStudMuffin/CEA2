name: "Fix Setup Instructions for Android SDK & Gradle Wrapper"
description: |
  ## Purpose
  Provide clear, ordered instructions so Codex can set up the Android SDK and Gradle wrapper without errors. The README will be updated to decode the wrapper, pull Git LFS assets, extract the command line tools, then install the SDK. The process confirms files exist at each step and finishes by configuring environment variables.

  ## Core Principles
  1. **Context is King**: reference official Android docs and existing scripts.
  2. **Validation Loops**: use Gradle commands to verify the environment works.
  3. **Information Dense**: show the exact paths and commands used in this repo.
  4. **Progressive Success**: verify each step before moving to the next.
  5. **Global rules**: follow repository guidelines in CODEX.md.

---

## Goal
A reliable setup process that leaves the repo ready to run `./gradlew` tasks and Android emulator tests.

## Why
- **Business value**: developers and Codex can build and test the app without manual troubleshooting.
- **Integration**: ensures scripts like `install_android_sdk.sh` work correctly.
- **Problem solved**: current instructions can fail if steps are executed in the wrong order.

## What
- Update `README.md` Setup section to clarify the order of operations.
- Confirm `commandlinetools-linux-13114758_latest.zip` exists after `git lfs pull`.
- Explicitly set `PATH` for `sdkmanager` before running the install script.
- Document creation of `local.properties` and environment variables.

### Success Criteria
- [ ] README shows the exact ordered commands.
- [ ] Following the README results in a working Gradle wrapper and installed SDK.
- [ ] `./gradlew lint` and tests run without missing SDK errors.

## All Needed Context

### Documentation & References
```yaml
- url: https://developer.android.com/studio/command-line
  why: explains `sdkmanager` usage and command line tools layout.
- url: https://developer.android.com/studio
  why: general installation guidance for Android Studio and SDK.
- file: README.md
  why: current setup instructions to be corrected.
- file: scripts/install_android_sdk.sh
  why: shows how the SDK is installed using the local archive.
```

### Current Codebase tree
```bash
.
├── AppFeatures.txt
├── CODEX.md
├── PRPs/
│   ├── EXAMPLE_multi_agent_prp.md
│   ├── bin_locator_bounding_box.md
│   └── executed_PRPs/
├── README.md
├── app/
├── commandlinetools-linux-13114758_latest.zip
├── scripts/
│   ├── decode_gradle_wrapper.py
│   └── install_android_sdk.sh
└── gradle/
    └── wrapper/
```

### Desired Codebase tree with files to be added and responsibility of file
```bash
PRPs/fix_setup_instructions.md  # this PRP
README.md                       # updated setup steps
```

### Known Gotchas of our codebase & Library Quirks
```bash
# The Gradle wrapper jar is stored as base64 and must be decoded before use.
# install_android_sdk.sh expects sdkmanager on PATH and the command line tools archive present.
# local.properties must point to the SDK or Gradle tasks will fail.
```

## Implementation Blueprint

### list of tasks to be completed to fullfill the PRP in the order they should be completed
```yaml
Task 1:
MODIFY README.md:
  - Clarify step order in Setup section.
  - Add check for commandlinetools zip after `git lfs pull`.
  - Provide example export of PATH for sdkmanager.
  - Mention running `scripts/decode_gradle_wrapper.py` before pulling LFS.
  - Document creating `local.properties` and setting ANDROID_HOME.

Task 2:
VERIFY scripts/install_android_sdk.sh:
  - Ensure instructions reference this script and its expected archive location.
```

### Per task pseudocode as needed added to each task
```bash
# Task 1 pseudocode
1. Open README.md
2. Reorder setup instructions:
   a. python scripts/decode_gradle_wrapper.py
   b. git lfs pull
   c. [ -f commandlinetools-linux-13114758_latest.zip ]
   d. unzip commandlinetools-linux-13114758_latest.zip -d android-tools
   e. export PATH="$PWD/android-tools/cmdline-tools/bin:$PATH"
   f. chmod +x scripts/install_android_sdk.sh && ./scripts/install_android_sdk.sh
   g. echo "sdk.dir=$ANDROID_HOME" > local.properties
3. Save and commit
```

### Integration Points
```yaml
SHELL:
  - update ~/.bashrc with ANDROID_HOME and PATH exports.
GRADLE:
  - local.properties ensures Gradle uses the installed SDK.
```

## Validation Loop

### Level 1: Syntax & Style
```bash
# README has no syntax check; verify scripts run
python scripts/decode_gradle_wrapper.py
ls gradle/wrapper/gradle-wrapper.jar
```

### Level 2: Unit Tests
```bash
./gradlew lint
./gradlew testDebugUnitTest
```

### Level 3: Instrumentation Tests
```bash
./gradlew connectedDebugAndroidTest
```

## Final validation Checklist
- [ ] All tests pass: `./gradlew testDebugUnitTest connectedDebugAndroidTest`
- [ ] No linting errors: `./gradlew lint`
- [ ] Manual setup following README succeeds without errors
- [ ] local.properties created and ANDROID_HOME exported

## Anti-Patterns to Avoid
- ❌ Do not skip decoding the wrapper jar.
- ❌ Do not run the SDK installer before extracting the tools archive.
- ❌ Do not assume environment variables are already set.

### PRP Confidence Score: 8/10
