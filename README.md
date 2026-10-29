# PitNudge Android

Early development version of **PitNudge**, a car-maintenance and trip-log app.

- Existing garage, reminders, service expenses, receipt images, and mechanic search are in the Android starter project archive.
- The GitHub Actions workflow extracts the archive and builds an Android **debug APK**.
- The debug app's GPS works only while the app remains in the foreground. Hands-free detection while the phone is locked is **not implemented**.
- Vehicle data is stored on the device; data in the previous browser prototype does not automatically sync.

## Build
Go to **Actions → Build PitNudge Android APK** to see runs. After a successful run, download the `PitNudge-debug-APK` artifact and extract `app-debug.apk`. This is for testing only, not Google Play.
