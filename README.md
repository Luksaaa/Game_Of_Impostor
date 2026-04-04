# Game of Impostor (Android Implementation)

This repository contains the initial Android-only release of Game of Impostor. A final, production-ready multi-platform version (supporting Android, iOS, Windows, and Web) is available in a separate repository.

## Project Overview

Game of Impostor is a real-time social deduction multiplayer application. Built with Kotlin and Jetpack Compose, it leverages Firebase Realtime Database to provide a synchronized environment for strategic gameplay based on verbal association and psychological deduction.

## Core Mechanics

The game supports between 2 and 16 players per session. Upon initiation, the system distributes roles using a weighted randomization algorithm:

*   **The Majority:** Participants receive the primary target word.
*   **The Imposter:** A participant receives a semantically related but distinct word, necessitating tactical listening to identify the majority's term.
*   **Mr. White:** A specialized high-difficulty role (20% probability). This participant receives no data and must extrapolate the game context entirely from other players' associations.

## Key Features

*   **Real-time Synchronization:** Low-latency state management powered by Firebase Realtime Database.
*   **Scalable Infrastructure:** Architecture optimized for up to 16 concurrent users per room.
*   **Administrative Suite:** 
    *   Configurable session timers (30s, 45s, 60s).
    *   Formalized voting and ejection system with automated win/loss state resolution.
*   **Accessibility & UX:**
    *   Secure room entry via 6-digit access codes or encrypted QR code deep links.
    *   "Hold to Confirm" safety protocols on critical session actions to prevent unintended data loss or exit.
    *   Full support for system-level Dark and Light modes using a custom aesthetic palette.

## Technical Specifications

*   **Language:** Kotlin 2.0+
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Backend:** Firebase Realtime Database
*   **Architecture:** Clean Architecture with decoupled data and UI layers.
*   **Integration:** 
    *   ZXing for high-fidelity QR generation.
    *   AndroidX Core Splashscreen API.
    *   Jetpack Navigation Compose.

## Installation & Deployment

1.  Clone the repository.
2.  Register the application package in your Firebase Console.
3.  Deploy the `google-services.json` configuration file to the `app/` directory.
4.  Configure Firebase Realtime Database security rules to allow appropriate read/write permissions.
5.  Build and deploy via Android Studio.

