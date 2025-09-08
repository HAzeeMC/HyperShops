#!/usr/bin/env bash
# Simple gradlew shim: requires gradle on PATH or replace with real wrapper.
if command -v ./gradlew >/dev/null 2>&1; then
  exec ./gradlew "$@"
elif command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
else
  echo "Gradle not found. Please install Gradle or replace this script with the real Gradle Wrapper."
  exit 1
fi
