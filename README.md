# PitNudge Android — v0.9 Bluetooth car-trigger beta

PitNudge remains an Android vehicle maintenance, account-synced garage, GPS mileage log, and optional route-map tracker.

## What's new in v0.9
- The **Trips → Bluetooth car trigger** option lists devices **already paired** in Android settings (requires Android 12+ Nearby devices / BLUETOOTH_CONNECT permission).
- Select your car's audio/Bluetooth connection once; that choice (device MAC address and display name) is stored **locally on this phone**, not added to your cloud garage or export.
- After **Keep auto-detect ON between drives** is enabled while PitNudge is visible, the existing ongoing Android foreground service watches connection/disconnection events for the chosen car. When the car connects, GPS starts watching for movement; after a disconnect, the app waits ~45 seconds to avoid short dropouts, finishes any active trip, and returns to waiting for the next Bluetooth connection.
- The connected car is checked on initial service start through A2DP/Headset Bluetooth profiles, so an existing car connection may be recognized even when monitoring starts after the car connects.
- **GPS-only auto-detect** continues to work with no paired car selected. Mileage and optional route maps still use GPS. Bluetooth does NOT read the vehicle's actual odometer.
- The existing status-bar tracking notification stays visible while auto monitoring is enabled. The car's Bluetooth state is shown on the Trips screen.

## Limitations / testing
Bluetooth connection events can vary by vehicle and Android device. Some car systems do not expose A2DP/headset profiles or reliable connection broadcasts; test with the actual car. The foreground monitor must first be started from the visible PitNudge app. It does **not** guarantee automatic starts after phone reboot, force-stop, Android battery restrictions, or if the service was stopped. Android 12+ background foreground-service restrictions, especially for location, prevent claiming that a disconnected app can always wake up on Bluetooth connection. Monitoring uses a persistent notification and can consume battery.

Bluetooth and device names are accessed only after the user's permission. Local paired-device addresses are not uploaded to cloud backups. Saved route maps can contain sensitive travel history; the route saving setting remains optional. The current prototype's separate Firebase cloud synchronization and map features are retained. Always export a backup before replacing a debug test build.

The GitHub Actions workflow assembles the baseline project and overlays v0.3 (cloud/photo), v0.5 (GPS mileage), v0.6 (tracking icon), v0.7 (routes), v0.8 (continuous monitoring fixes), and v0.9 (Bluetooth trigger), then builds the debug APK.
