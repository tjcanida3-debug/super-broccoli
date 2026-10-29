# PitNudge Android — v0.8 beta

Vehicle maintenance, mileage, receipts, Google sign-in and cloud sync with opt-in GPS trip tracking.

## v0.8: Automatic tracking across drives
- The **Keep auto-detect ON between drives** setting is an explicit opt-in.
- When turned on in a visible Activity, Android uses a foreground location service with an ongoing notification. A drive finishes automatically after about 3 minutes stationary. **The monitor stays on** after the trip for the next drive, until the user explicitly turns it off.
- The **Finish current drive** action saves an ongoing trip while retaining the auto-monitoring service. The separate **Turn off auto-detect** action stops it.
- If Android kills the service, opening the app tries to resume the previously opted-in monitoring. If the user explicitly stops it via the notification, the app honors that choice and does not auto-restart.
- This is not guaranteed 24/7 passive background detection. Reboots, force-stops, battery optimization, lost permissions and OS background restrictions may interrupt monitoring. A persistent foreground GPS monitor may use substantial battery. The app does not silently start background location tracking without a foreground user action.

## v0.8: Fix missing trip maps
- The Android service captures valid GPS route points independently of the mileage speed threshold, only when **Save trip route maps** is enabled.
- The app reconciles its saved route preference with the native Android service and shows **Map saving ON/OFF** plus the number of GPS points captured, so users can verify it is actually recording coordinates before driving.
- **View map** appears for a saved trip only when at least two GPS points were captured. Trips saved before route capture was active cannot be retroactively mapped.
- Routes can be removed separately from mileage; if cloud sync is enabled, route coordinates in trip logs may be included in cloud backups. Map tiles require internet access.
- GPS distance remains an estimate; compare it with your actual vehicle odometer.

## Build
GitHub Actions extracts the Android base source ZIP and applies source overlays (v0.3 Firebase/photo, v0.5 mileage, v0.6 status icon, v0.7 route maps, and v0.8 fixes), then builds a debug APK. Launcher icon is unchanged. GitHub Actions build success does not establish GPS behavior on a specific phone; test on an Android device.
