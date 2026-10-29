# PitNudge Android

**v0.3 experimental Android APK** — vehicle maintenance, mileage logs, and example vehicle photos.

The Android build uses the baseline project archive plus the `v03-overlay/` source files, which are applied by the GitHub Actions workflow. The HTML is divided into several plain-text parts because the source archive transfer exceeded the connector's size/safety limit.

**Vehicle photos:** When a network connection is available, the app searches Wikimedia Commons for an image whose filename and metadata match the selected make and model and whose metadata identifies a permitted reuse license. Photos may depict a nearby year or different color, and some models will not have a photo. Author and license links are displayed. The app falls back to a locally bundled illustration for unmatched models or offline use. User-selected photos take precedence. Review image licenses before any public release.

**Still experimental:** Google and Apple sign-in and actual cloud backups require Firebase configuration; automatic locked-screen mileage detection is not implemented. Test with non-sensitive vehicle information. For testing, keep JSON backups before uninstalling.
