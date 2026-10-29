# PitNudge Android

**v0.5 — GPS Mileage Tracking Test Build**

The Android app supports opted-in **manual GPS trips and drive auto-detection**, including while the screen is locked, through an Android foreground location service with a visible notification. To begin monitoring, open PitNudge and tap **Trips → Start GPS trip** or switch on **Auto-detect driving**. Automatic detection is not enabled around the clock: you must turn it on before a drive. Auto-detected trips end after ~3 minutes stationary. Manual trips end using **Finish trip** or the tracking notification.

The Android service stores estimated distance and trip metadata locally and queues completed trips until the WebView imports them. It does not store raw routes. Completed trips update the selected car's **estimated** odometer once and can sync through Firebase if the user has enabled it. GPS cannot read the real vehicle odometer, and accuracy depends on signal and location permissions. Back up garage data before replacing debug APKs.

This public repository retains the earlier baseline archive, the v0.3 vehicle-photo and configured Firebase overlay, and the v0.5 GPS-mileage overlay. GitHub Actions reconstructs the sources and builds a debug APK. The test signing identity is preserved using Actions cache when available.

Photo licensing and attribution still apply. Apple sign-in, 24/7 passive driving detection, and production release signing are not complete.
