# iPhone implementation

EyeType now has a native SwiftUI host app and a native iOS Keyboard Extension target described by `project.yml`.

## How gaze interaction works on iPhone

iOS does not allow a third-party keyboard extension to freely use the camera. The iPhone keyboard therefore relies on Apple's built-in Accessibility Eye Tracking and its pointer/dwell interaction to activate the accessible keyboard buttons. EyeType does not request Full Access or camera permission in the keyboard extension.

## Build

The GitHub Actions workflow uses a macOS runner and XcodeGen to generate the Xcode project, then builds the app and keyboard extension for the iOS Simulator without code signing.

A simulator build is not installable on a physical iPhone. Installing on a real device requires Apple signing/provisioning, and accessibility behavior must be tested on the actual iPhone and iOS version.

## Manual setup on iPhone

1. Install the signed EyeType host app and keyboard extension.
2. Open Settings → General → Keyboard → Keyboards → Add New Keyboard → EyeType.
3. Open Settings → Accessibility → Eye Tracking and complete Apple's setup.
4. Switch to EyeType from the globe key in a text field.
5. Verify that Apple's eye pointer/dwell interaction activates the keyboard buttons.

No camera frames or typed text are uploaded by this keyboard.
