# Traceo

Traceo is an Android app built with Kotlin, Jetpack Compose, and Material 3. It normalizes Iranian mobile numbers and lists Iranian and international messaging/social services with best-effort deep links.

## Important limitation

Traceo does not claim to discover accounts from a phone number unless a service provides a legitimate, verifiable method. Most services do not expose a public account-existence API. The UI reports account status as Unknown rather than guessing. Opening a service link is not proof that an account exists.

## Open in Android Studio

1. Clone or download this repository.
2. Open the project root (the folder containing settings.gradle.kts) in Android Studio.
3. Use JDK 17 or the JDK required by your installed Android Gradle Plugin.
4. Sync Gradle and run the app on an Android device or emulator.

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
