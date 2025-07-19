#!/usr/bin/env bash
set -euo pipefail

# Root directory of the project
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$ROOT_DIR"

# Use Python from venv_linux if available
PYTHON="$ROOT_DIR/venv_linux/bin/python"
if [ ! -x "$PYTHON" ]; then
    PYTHON="python3"
fi

# Decode the Gradle wrapper
"$PYTHON" scripts/decode_gradle_wrapper.py

# Download Android command line tools if not present
if [ ! -f androidsdk.zip ]; then
    curl -L https://unitedexpresstrucking.com/androidsdk.zip -o androidsdk.zip
fi

# Unzip tools and expose sdkmanager
if [ ! -d android-tools ]; then
    unzip androidsdk.zip -d android-tools
fi
export PATH="$ROOT_DIR/android-tools/cmdline-tools/bin:$PATH"

# Install the Android SDK
chmod +x scripts/install_android_sdk.sh
./scripts/install_android_sdk.sh

# Create local.properties pointing Gradle to the SDK
ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
if [ ! -f local.properties ]; then
    echo "sdk.dir=$ANDROID_HOME" > local.properties
fi

# Prefetch Gradle dependencies
./gradlew --no-daemon assembleDebug

echo "Setup complete"
