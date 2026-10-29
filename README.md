# PitNudge Android

Android debug application for vehicle maintenance, trip logs, nearby mechanic searches and expense receipts.

**Version 0.1.3** adds an account screen and *backend-ready* Firebase Authentication and Firestore code, but **Google/Apple login and cloud backup are disabled until PitNudge's own Firebase project, Google OAuth setup, Apple developer configuration, Firestore security rules, and stable APK signing have been configured and tested.** No user records are uploaded from the unconfigured build. The app continues working locally without an account.

The GitHub Actions workflow packages the source archive into an Android debug APK. This is **not** a Play Store release. Foreground GPS is experimental; automatic background driving detection is not implemented.

See `ACCOUNT_SETUP.md` inside the project ZIP for setup instructions and data safety limitations. Receipt images exceeding Firestore's snapshot limit need Cloud Storage integration before full backup can be promised.
