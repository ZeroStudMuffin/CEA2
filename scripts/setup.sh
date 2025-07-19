#!/usr/bin/env sh
# POSIX-compliant project setup for Codex environment
set -eu

# 1. Set Android SDK root and export environment variable
ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
export ANDROID_HOME
echo "Using Android SDK root: $ANDROID_HOME"

# 2. Decode the Gradle wrapper
echo "Decoding Gradle wrapper..."
python scripts/decode_gradle_wrapper.py

# 3. Download Android SDK archive if missing
if [ ! -f androidsdk.zip ]; then
  echo "Downloading Android SDK tools..."
  curl -L https://unitedexpresstrucking.com/androidsdk.zip -o androidsdk.zip
else
  echo "Using existing androidsdk.zip"
fi

# 4. Extract SDK tools to android-tools directory
echo "Extracting Android SDK tools to android-tools/..."
rm -rf android-tools
mkdir -p android-tools
unzip -q androidsdk.zip -d android-tools

# 5. Temporarily expose sdkmanager from extracted tools
export PATH="$PWD/android-tools/cmdline-tools/bin:$PATH"
echo "Temporarily updated PATH for sdkmanager"

# 6. Run the installer to set up SDK and build-tools
echo "Running install_android_sdk.sh..."
chmod +x scripts/install_android_sdk.sh
scripts/install_android_sdk.sh

# 7. Persist PATH to include installed SDK tools and platform-tools
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
echo "Updated PATH for Android SDK tools and platform-tools"

# 8. Configure Gradle to use the local SDK
echo "sdk.dir=$ANDROID_HOME" > local.properties

# 9. Install emulator packages for instrumentation tests
echo "Installing emulator system images and emulator package..."
yes | sdkmanager "system-images;android-34;google_apis;x86_64" "emulator"

# 10. Final confirmation
echo "Setup complete!"
