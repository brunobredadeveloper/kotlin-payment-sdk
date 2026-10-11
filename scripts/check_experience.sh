#!/usr/bin/env bash
set -euo pipefail
collect() {
 mkdir -p ux-screenshots
 adb pull /sdcard/Android/data/dev.breda.payments.sample/files/ux ux-screenshots/ || true
}
trap collect EXIT
./gradlew -PincludeAndroid=true :sample-android:connectedDebugAndroidTest --stacktrace
