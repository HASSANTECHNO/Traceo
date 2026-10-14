# Traceo

Traceo is an Android app built with Kotlin, Jetpack Compose, and Material 3. It normalizes Iranian mobile numbers and lists Iranian and international messaging/social services with best-effort deep links.

## Important limitation

Traceo does not claim to discover accounts from a phone number unless a service provides a legitimate, verifiable method. Most services do not expose a public account-existence API. The UI reports account status as Unknown rather than guessing. Opening a service link is not proof that an account exists.

## Build and test

The repository includes the Gradle Wrapper, pinned to Gradle 9.8.1. Open the project root (the folder containing `settings.gradle.kts`) in Android Studio and sync Gradle with JDK 17.

On Windows, run:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

On Linux or macOS, run:

```bash
./gradlew testDebugUnitTest assembleDebug
```

GitHub Actions runs the same tests and build and publishes the debug APK as the `traceo-debug-apk` workflow artifact.

## Current scope

- Iranian number normalization to E.164 (+98...).
- Service list grouped into Iranian and international categories.
- Best-effort open links for WhatsApp, Telegram, and Viber.
- No phone number is sent to a Traceo server.

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- Android Gradle Plugin 9.4.0
- Kotlin 2.4.10
