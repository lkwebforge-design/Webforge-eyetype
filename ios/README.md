# iPhone keyboard implementation

The iPhone build will use a native iOS Keyboard Extension (custom keyboard) for system-wide text entry.

Important platform constraint: third-party keyboard extensions cannot freely access the camera. EyeType will therefore investigate Apple's built-in Accessibility Eye Tracking / pointer control to activate keys in the extension. This must be validated on supported iPhone models and iOS versions.

## Xcode setup
1. Create an iOS App target named EyeType.
2. Add a Keyboard Extension target named EyeTypeKeyboard.
3. Add `EyeTypeKeyboardViewController.swift` to that extension target.
4. Keep `RequestsOpenAccess = false`; the keyboard does not need full access to type text.
5. Test with Settings → Accessibility → Eye Tracking on supported hardware.

This source starter is not yet a signed or installable iOS app.
