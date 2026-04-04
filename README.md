# 🕵️ Game of Impostor (Android Version)

> **⚠️ Note:** This is the **Android-only** initial version of the game. For the final, **multi-platform** version (supporting Android, iOS, Windows, and Web), please check out our [Final Repository](https://github.com/your-username/imposter-game-multiplatform).

A modern, real-time social deduction game built with **Android Jetpack Compose** and **Firebase Realtime Database**. Gather your friends, describe your secret words, and find out who among you is the master of deception!

## 🎮 Overview

In **Game of Impostor**, players are assigned secret words. Most players (The Majority) get the same word, but one player (The Imposter) receives a slightly different pojam. There is also a 20% chance that a **Mr. White** joins the game—a player who has no word at all and must rely entirely on listening to others to blend in.

## ✨ Features

*   **Real-time Multiplayer:** Instant synchronization across all devices using Firebase.
*   **Up to 16 Players:** Large lobby support for big groups.
*   **Roles System:** 
    *   **The Majority:** Have the main secret word.
    *   **The Imposter:** Have a similar but different word.
    *   **Mr. White:** Have no word at all (High difficulty!).
*   **In-game Chat:** Grouped messages and modern UI for effective discussion.
*   **Discussion Timer:** Admin can start a 30, 45, or 60-second timer to keep the game moving.
*   **QR Code Lobby Entry:** Generate a QR code in the lobby for friends to scan and join instantly via deep links.
*   **Voting Mechanism:** Admin-led ejection system with automatic win/loss detection.
*   **Aesthetic Design:** Adaptive theme (Sage Green & Muted Rose) that looks great in both Dark and Light modes.
*   **Safety First:** "Hold to Confirm" (2s) on critical buttons like "Repeat Game" or "Leave Room" to prevent accidental clicks.

## 🚀 How to Play

1.  **Create or Join a Room:** Start a new room as an Admin or enter a 6-digit code (or scan a QR) to join.
2.  **Describe:** Each player says exactly **ONE** word (association) that describes their pojam.
    *   *Majority:* Be clear to your team, but vague enough to confuse the Imposter.
    *   *Imposter:* Try to figure out the Majority's word and mimic their descriptions.
    *   *Mr. White:* Listen carefully and guess the topic!
3.  **Forbidden:** Never say the secret word itself or its roots!
4.  **Discuss:** Use the in-game chat or talk in person.
5.  **Vote:** The Admin starts the vote. Point out the suspect and eject them.
6.  **Goal:** 
    *   *Majority:* Eject the Imposter/Mr. White before they figure out your word.
    *   *Imposter/Mr. White:* Blend in and survive until the end.

## 🛠 Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Backend:** Firebase Realtime Database
*   **Architecture:** Clean architecture with dedicated Managers (Firebase, WordManager)
*   **Utilities:** ZXing (QR generation), Splash API, Navigation Compose

## 📦 Setup

1.  Clone the repository.
2.  Connect the project to your own **Firebase Project**.
3.  Add the `google-services.json` file to the `app/` directory.
4.  Enable **Anonymous Authentication** or handle user names as implemented.
5.  Build and Run!

---
Created with ❤️ for fans of social deduction games.
