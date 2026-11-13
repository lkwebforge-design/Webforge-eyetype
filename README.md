# EyeType

EyeType is an accessibility-first mobile keyboard project. The goal is to let people compose text by looking at keys, with a platform-appropriate implementation on Android and iPhone.

## Product requirements

- System keyboard integration where the operating system permits it.
- Gaze-based key selection with configurable dwell time (initial target: 500 ms).
- Calibration and tracking-quality feedback.
- Robust behavior in ordinary indoor light, dim conditions, and bright conditions; never silently type when tracking confidence is poor.
- No text-to-speech in the MVP.
- Privacy-first: camera frames and eye landmarks should be processed on-device and never uploaded.

## Platform strategy

### Android
A native Android app plus an Input Method Editor (IME). The IME is the system keyboard. A separately managed, user-visible camera/tracking component will provide gaze coordinates to the selection engine while Android's camera and foreground-service rules are respected.

### iPhone
A native keyboard extension can provide system-wide text entry, but iOS does not allow a third-party keyboard extension to freely use the camera. The iPhone plan is to use Apple's built-in Accessibility Eye Tracking / pointer-control facilities to interact with the EyeType keyboard where supported. We must validate this on real devices before promising a universal experience.

## Current milestone: foundation
This first source milestone establishes:
- Android project structure.
- Android IME registration and a working touch keyboard baseline.
- A gaze-selection controller interface with dwell timing, ready for a real gaze estimator.
- iOS keyboard-extension starter source and setup notes.

**This milestone is not yet an eye-tracking release.** Camera calibration, gaze estimation, lighting tests, iOS hardware compatibility testing, and installable signed builds remain to be completed.

## Build Android

Open the repository in Android Studio and use JDK 17 with Android SDK 35. If Gradle 8.9 is installed:

```bash
gradle :app:assembleDebug
```

A GitHub Actions workflow is included to build the Android debug APK automatically on pushes to `main`.

## Safety and accessibility
EyeType must always provide a clear pause/disable path, a touch fallback, visible tracking status, and a way to undo accidental input. It must not claim reliable operation in complete darkness or direct glare. If tracking quality is poor, pause gaze selection and ask the user to adjust lighting or recalibrate.

## License
To be decided.
