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
The current Android development milestone establishes:
- Android IME registration and a touch keyboard with letter, shift, number/symbol, space, backspace, and enter controls.
- On-device MediaPipe Face Landmarker integration for face/iris landmark estimates.
- A user-started CameraX foreground service with a persistent stop notification; camera frames are processed locally.
- Five-point gaze calibration stored locally on the device.
- A 500 ms dwell selector connected to the keyboard, gated by calibration and tracking confidence.
- iOS keyboard-extension starter source and setup notes.

**Important:** this is still an engineering prototype, not a validated release. Gaze mapping accuracy, camera orientation, lighting adaptation, battery use, and false selections must be tested on real Android devices before relying on it. The current estimator is an initial iris-position approach, not a clinically validated eye tracker. The iPhone extension still needs a real Xcode target, accessibility integration, and device testing.

## Build Android

Open the repository in Android Studio and use JDK 17 with Android SDK 35. If Gradle 8.9 is installed:

```bash
gradle :app:assembleDebug
```

A GitHub Actions workflow downloads the official MediaPipe Face Landmarker model and builds the Android debug APK automatically on pushes to `main`. The model is packaged into the APK so tracking does not need to upload camera frames or fetch a model at runtime.

## Safety and accessibility
EyeType must always provide a clear pause/disable path, a touch fallback, visible tracking status, and a way to undo accidental input. It must not claim reliable operation in complete darkness or direct glare. If tracking quality is poor, pause gaze selection and ask the user to adjust lighting or recalibrate.

## License
To be decided.
