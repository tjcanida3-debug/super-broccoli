# PitNudge v0.9.1 — optional Bluetooth car trigger

A test Android APK building on v0.9. Retains GPS mileage, auto-detect, map routes, Google sign-in and original app icon. New optional Bluetooth connection-based GPS monitoring can watch a previously paired car. The service must be started from PitNudge while foreground; Android background restrictions may prevent reactivation after a force-stop or reboot. 45-second grace after car disconnect, retaining notification for next drive. When car remains connected, a long traffic light does not end the trip; the Bluetooth broadcast watcher also registers even when Bluetooth is initially switched off. GPS-only auto-detect remains an option.

Nearby-devices permission (BLUETOOTH_CONNECT) is required on Android 12+. This does not control the car or read its odometer. Real car testing required, and users should compare GPS distance with dashboard mileage.
