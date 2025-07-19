#!/usr/bin/env sh
# POSIX-compliant Android SDK installer for Codex environment
set -eu

# 1. Accept all SDK licenses before installing
echo "Accepting all Android SDK licenses..."
yes | sdkmanager --licenses >/dev/null 2>&1

# 2. Configure SDK root
ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
TOOLS_DIR="$ANDROID_HOME/cmdline-tools"
LATEST_DIR="$TOOLS_DIR/latest"

# 3. Locate project root & zip
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
ROOT_DIR=$(dirname "$SCRIPT_DIR")
cd "$ROOT_DIR"
LOCAL_ZIP="$ROOT_DIR/androidsdk.zip"

# 4. Prepare directories
mkdir -p "$LATEST_DIR"
TMP_DIR=$(mktemp -d)
trap 'rm -rf "$TMP_DIR"' EXIT

# 5. Download or use local archive
if [ -f "$LOCAL_ZIP" ]; then
  echo "Using local Android SDK archive..."
  UNZIP_SRC="$LOCAL_ZIP"
else
  echo "Downloading Android SDK command-line tools..."
  DOWNLOAD_URL="${ANDROID_SDK_URL:-https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip}"
  curl -L "$DOWNLOAD_URL" -o "$TMP_DIR/sdk.zip"
  UNZIP_SRC="$TMP_DIR/sdk.zip"
fi

# 6. Extract
echo "Extracting to temporary directory..."
unzip -q "$UNZIP_SRC" -d "$TMP_DIR"
if [ -d "$TMP_DIR/cmdline-tools" ]; then
  SRC_DIR="$TMP_DIR/cmdline-tools"
else
  SRC_DIR="$TMP_DIR/tools"
fi
mv "$SRC_DIR"/* "$LATEST_DIR/"

# 7. Update PATH
echo "Exporting environment variables: ANDROID_HOME and PATH"
export ANDROID_HOME
export PATH="$LATEST_DIR/bin:$ANDROID_HOME/platform-tools:$PATH"
echo "Android SDK root set to $ANDROID_HOME"

# 8. Install SDK packages
echo "Installing core SDK packages..."
for pkg in \
  "platform-tools" \
  "platforms;android-34" \
  "build-tools;34.0.0"; do
  echo "Installing $pkg..."
  sdkmanager --install "$pkg"
done

# 9. Completion message
echo "Android SDK installation complete at $ANDROID_HOME"
