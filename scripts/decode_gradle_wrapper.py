#!/usr/bin/env python3
"""Decode the Gradle wrapper jar from base64."""
import base64
from pathlib import Path

BASE64_PATH = Path("gradle/wrapper/gradle-wrapper.jar.base64")
JAR_PATH = Path("gradle/wrapper/gradle-wrapper.jar")

def main() -> None:
    data = base64.b64decode(BASE64_PATH.read_text())
    JAR_PATH.write_bytes(data)

if __name__ == "__main__":
    main()
