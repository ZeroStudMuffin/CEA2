#!/usr/bin/env bash
set -euo pipefail

ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
TOOLS_DIR="$ANDROID_HOME/cmdline-tools"
LATEST_DIR="$TOOLS_DIR/latest"

mkdir -p "$LATEST_DIR"

# Fetch latest command line tools download URL
REPO_XML_URL="https://dl.google.com/android/repository/repository2-1.xml"
DOWNLOAD_URL=$(curl -s "$REPO_XML_URL" | grep -o 'https://dl.google.com/android/repository/commandlinetools-linux-[0-9]*_latest.zip' | head -n 1)

echo "Downloading Android command line tools..."
mkdir -p /tmp/android-tools
cd /tmp/android-tools
curl -L "$DOWNLOAD_URL" -o tools.zip
unzip -q tools.zip
mv cmdline-tools/* "$LATEST_DIR/"
cd - >/dev/null
rm -rf /tmp/android-tools

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
