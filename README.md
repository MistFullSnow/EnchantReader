# EnchantReader (Android)

A modern, immersive Android reader for your own PDF of "Harry Potter and the Cursed Child". It extracts Acts and Scenes, then presents them with a glass-like UI, animated backgrounds, and an intuitive table of contents.

Important: This app does not ship copyrighted content. Supply your own legally obtained PDF on-device using the file picker.

## Features
- File picker to load your local PDF (no extra permissions needed)
- PDF parsing powered by PDFBox-Android
- Act/Scene detection via regex
- Jetpack Compose Material 3 + glassmorphism
- Animated particle background
- Scene navigator and full-screen reading view
- GitHub Actions CI builds and uploads a debug APK artifact

## Build on GitHub (recommended)
1. Create a new GitHub repository (public or private).
2. Copy/push this `EnchantReader` folder as the repository root, including the `.github/workflows/android.yml`.
3. Push to `main`.
4. Go to the Actions tab, open the running workflow, and wait for it to finish.
5. Download the uploaded artifact named `EnchantReader-debug-apk` and install it on your device.

## Using the app on device
- Open the app, tap "Select PDF", and choose your local copy of the book.
- The app parses the PDF, builds a table of contents, and presents Acts/Scenes.
- Tap a Scene to start reading with animations, glass panels, and themed styling.

## Notes
- PDF structure and formatting can vary. If no clear Acts/Scenes are found, the whole text is presented as a single scene.
- All images/visuals are generated at runtime; no copyrighted assets are bundled.
