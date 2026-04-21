# AdivinaBandera

[![CI](https://github.com/AlvaroQ/AdivinaBandera/actions/workflows/ci.yml/badge.svg)](https://github.com/AlvaroQ/AdivinaBandera/actions/workflows/ci.yml)
[![Google Play](https://img.shields.io/badge/Google%20Play-Download-0F9D58?logo=google-play&logoColor=white)](https://play.google.com/store/apps/details?id=com.alvaroquintana.adivinabandera)
![API Level](https://img.shields.io/badge/API-23%2B-brightgreen)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2026.03.01-4285F4)
![Unit tests](https://img.shields.io/badge/unit%20tests-204%20passing-22C55E)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

---

## Table of Contents

[About](#about) · [Screenshots](#screenshots) · [Game Modes](#game-modes) · [Tech Stack](#tech-stack) · [Architecture](#architecture) · [Features](#features) · [Design decisions](#design-decisions) · [Testing](#testing) · [Getting Started](#getting-started) · [Roadmap](#roadmap) · [Links](#links) · [License](#license)

---

## About

AdivinaBandera is an Android quiz game about **world flags, capitals, currencies, populations and regional symbols**. It started as a small hobby app in 2019 and has grown into a 200+ country catalogue with 11 game modes, a progression system, a cosmetics economy and daily challenges — built with modern Kotlin, Jetpack Compose and Clean Architecture.

It is also a long-lived production codebase that doubles as a real-world reference for modular Android architecture, Koin dependency injection, Firebase-backed leaderboards and a fully offline-first data layer powered by Room.

---

## Screenshots

<table align="center">
  <tr>
    <td align="center"><img src="capture/es/image1.jpg" width="180"><br/><sub>Game mode selection</sub></td>
    <td align="center"><img src="capture/es/image2.jpg" width="180"><br/><sub>Flag question in play</sub></td>
    <td align="center"><img src="capture/es/image3.jpg" width="180"><br/><sub>Capital by flag</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="capture/es/image4.jpg" width="180"><br/><sub>Result & XP earned</sub></td>
    <td align="center"><img src="capture/es/image5.jpg" width="180"><br/><sub>Global ranking</sub></td>
  </tr>
</table>

---

## Game Modes

Eleven progressively-unlocked modes keep sessions varied beyond "guess the flag":

| Mode                    | Unlock        | What you guess                                                    |
| ----------------------- | ------------- | ----------------------------------------------------------------- |
| **Classic**             | Level 1       | Country from its flag                                             |
| **Capital by flag**     | Level 1       | Capital city from the flag                                        |
| **Currency Detective**  | Level 5       | Country from its currency                                         |
| **Population Challenge**| Level 10      | Higher-population country between two options                     |
| **World Mix**           | Level 15      | Random mix of 7 question types (language, demonym, neighbours...) |
| **Regional — Spain**    | Always        | Autonomous communities by flag                                    |
| **Regional — Mexico**   | 6 correct ES  | Mexican states by flag                                            |
| **Regional — Argentina**| 6 correct MX  | Argentine provinces by flag                                       |
| **Regional — Brazil**   | 6 correct AR  | Brazilian states by flag                                          |
| **Regional — Germany**  | 6 correct BR  | German federal states by flag                                     |
| **Regional — USA**      | 6 correct DE  | US states by flag                                                 |

Each mode keeps its own Firestore leaderboard (top 20) and contributes independently to XP and daily challenge progress.

---

## Tech Stack

| Category               | Technology                                                      | Version           |
| ---------------------- | --------------------------------------------------------------- | ----------------- |
| Language               | Kotlin                                                          | 2.3.20            |
| Build                  | Android Gradle Plugin                                           | 9.1.1             |
| UI                     | Jetpack Compose + Material3                                     | BOM 2026.03.01    |
| Architecture           | Clean Architecture — 4 Gradle modules                           | MVVM              |
| State Management       | StateFlow + SharedFlow                                          | Coroutines 1.10.2 |
| Navigation             | Navigation Compose (type-safe, kotlinx.serialization)           | 2.9.7             |
| Dependency Injection   | Koin (Android + Compose)                                        | 4.2.1             |
| Local Persistence      | Room (KSP) + DataStore Preferences                              | 2.8.4 / 1.2.1     |
| Background work        | WorkManager (daily reminders)                                   | 2.11.2            |
| Backend                | Firebase (Firestore, Realtime DB, Auth, Analytics, Crashlytics) | BOM 34.12.0       |
| Images                 | Coil Compose (+ SVG decoder)                                    | 3.4.0             |
| Serialization          | kotlinx.serialization                                           | 1.11.0            |
| Monetization           | AdMob (banner + rewarded) with UMP consent                      | 25.2.0 / 4.0.0    |
| Min SDK                | Android 6.0 (Marshmallow)                                       | API 23            |
| Compile / Target SDK   | Android 15                                                      | API 36            |

---

## Architecture

<p align="center">
  <img src="docs/architecture.svg" alt="Clean Architecture — 4 Gradle modules with bento-grid layout" width="720">
</p>

Four Gradle modules, one responsibility each:

- **`app`** — Android layer: Compose screens, ViewModels, Koin modules, DataSource implementations, notification scheduling.
- **`usecases`** — Pure-JVM orchestration (`GetCountryUseCase`, `ProcessGameResultUseCase`, `XpLeaderboardUseCases`...).
- **`data`** — Repository and DataSource **interfaces**. No implementations — those live in `app` so the Android SDK stays out of `data`.
- **`domain`** — Pure Kotlin entities: `Country`, `Subdivision`, `Streak`, `DailyChallenge`, `PlayerCosmetics`, `XpLeaderboardEntry`. Zero Android imports.

The dependency rule is enforced by the Gradle graph itself: if `domain` tried to import anything Android, Gradle wouldn't compile it. ViewModels expose `StateFlow<UiState>` for reactive state and `SharedFlow<UiEvent>` for one-shot events (navigation, dialogs). Koin wires everything at application start.

---

## Features

### Gameplay
- 11 game modes with level-gated unlocks (see [Game Modes](#game-modes))
- Extra lives system — earn up to three lives per game, lose one on every wrong answer
- In-game streak counter with motivational messages at 3, 5, 10, 15 and every 5 correct

### Progression
- **50 XP levels**, dynamic titles from *Novato* to *Leyenda*, progressive thresholds up to 393,000 XP
- **Daily streaks** with freeze-token mechanic to save a broken streak
- **3 daily challenges** regenerated at local midnight, deterministic per user + date so two devices of the same player show the same challenges
- **20+ achievements** covering games played, accuracy, streaks, perfect games and milestone daily streaks

### Economy & cosmetics
- **Dual currency**: *coins* (common, earned from play) and *gems* (rare, awarded for achievements and streaks)
- **17 unlockable cosmetics** across 5 categories: profile frames, title badges, answer card themes, celebration animations, alternative app icons
- **Mystery Box** awarded every 10 games completed — XP boost, coin bonus or freeze token
- **Country Mastery** tracking — per-country accuracy and last-seen timestamp persisted via Room, used to surface weaker flags more often

### Content & data
- 200+ countries with name, capital, currency, language, demonym, borders, population, area, calling codes and translations
- Regional subdivisions for 6 countries with dedicated flag sets
- Offline-first: Room database seeded from a Firebase Realtime Database sync on first run; subsequent plays work without network

### Platform
- Light / Dark theme with Material3
- Spanish and English localization
- AdMob **banner** and **rewarded** ads (consent handled via Google's UMP SDK)
- Daily reminder notification scheduled through WorkManager (`ExistingPeriodicWorkPolicy.KEEP`)

---

## Design decisions

Short rationale behind the less-obvious architectural choices. Every decision is a tradeoff — these notes explain what was gained and what was given up.

### Room as local source of truth, Firebase as the seed

Country and subdivision data is **synced once** from Firebase Realtime Database into a Room database, and every subsequent query hits Room. This means:

- The quiz is fully playable offline after the first launch.
- Random-draw queries (`GetRandomSubdivisions`, country lookups by calling code or language) run on SQLite instead of issuing Firestore reads — dramatically cheaper at scale and with predictable latency.
- A `SyncMetadata` row tracks the last successful sync, so incremental updates can be pulled without re-downloading everything.

**Tradeoff:** new content isn't instantly available — a player sees changes on their next sync, not in real time. For a quiz about world flags, that's a fair compromise: the dataset changes on the order of weeks or months, not minutes.

### XP is local, leaderboards are public

User XP, coins, gems, streaks, achievements and cosmetic unlocks all live in **DataStore on-device only**. The **only** data that leaves the device is an anonymous, pseudonymous top-20 leaderboard entry per game mode, stored in Firestore (`ranking-classic`, `ranking-capital-by-flag`, `ranking-worldmix`, one per regional mode…).

This keeps the app GDPR-simple (no personal data persisted server-side), keeps Firestore cost bounded (reads are capped at 20 per mode per ranking view) and avoids the entire "account recovery" surface. Players authenticate anonymously via Firebase Auth — no email, no password, no social login.

**Tradeoff:** a player who reinstalls the app or changes device loses their progress. Accepted: the game is free, sessions are short, and the compensation is zero friction on first launch and zero privacy footprint.

### Deterministic daily challenges

The 3 daily challenges for a given user on a given date are derived from a **deterministic hash** of `installId + date`, not from a random roll. Two devices of the same user show the same challenges, challenges are stable across app restarts, and the UI can safely show "already completed" state without needing a server-side source of truth.

**Tradeoff:** challenges are predictable to a player who datamines the catalog. Acceptable — the catalog is small enough that variation comes from the date rotation, not from obscurity.

### Koin over Hilt

Picked for faster iteration on a Kotlin-first, Compose-heavy codebase. No `kapt` / `ksp` in the DI path means shorter incremental builds, and the composable-friendly API (`koinInject()`, `koinViewModel()`) doesn't need annotation processing to reach into the UI layer. The only KSP compiler in use is Room's, which is unavoidable.

**Tradeoff:** Koin resolves graphs at runtime, so a missing binding surfaces as a crash on first use rather than a red squiggle. Partial mitigation: the unit-test suite in every module exercises the DI graph for the happy-path dependencies.

### Progressive mode unlocks

Levels 5, 10 and 15 gate advanced modes (Currency Detective, Population Challenge, World Mix), and the regional chain requires 6 correct answers in the previous country's subdivisions to unlock the next. This is pure retention design — a new player isn't overwhelmed by 11 mode tiles on day one, and each unlock is a small dopamine hit that rewards continued play.

**Tradeoff:** a seasoned player returning after an update may be frustrated to re-earn modes they knew. Acceptable because progression is persisted across sessions on the same install, and unlocks happen within the first few sessions.

---

## Testing

<p align="center">
  <img src="docs/test-coverage.svg" alt="Unit test distribution across 4 modules: 204 tests, all passing" width="720">
</p>

All tests run on the JVM — no device, no emulator. Every push and pull request to `main` runs the full suite through [GitHub Actions](.github/workflows/ci.yml).

| Module     | Tests | What's covered                                                                          |
| ---------- | ----- | --------------------------------------------------------------------------------------- |
| `app`      | 120   | ViewModels (`GameViewModel`, `ResultViewModel`, `RankingViewModel`, `InfoViewModel`), `ProgressionManager`, `ChallengeAppConfig`, `BanderaCatalog`, `DataBaseSourceImpl` subdivisions |
| `domain`   | 48    | `StreakRules` (37 tests — streak progression and freeze-token logic), `ChallengeReward` payout math |
| `usecases` | 22    | `GetCountryUseCase`, `GetRandomSubdivisions`, `GetRankingScore`, `GetRecordScore`, `SaveTopScore` |
| `data`     | 14    | `CountryRepository`, `RankingRepository` contracts                                      |
| **Total**  | **204** | **0 failures · 0 flaky · 0 skipped**                                                  |

Stack:

- **JUnit 4** for the test harness
- **MockK 1.14.9** for mocking coroutine APIs and Firebase boundaries
- **Turbine 1.2.1** for asserting `Flow` emissions
- **kotlinx.coroutines.test** for `runTest` and `TestDispatcher`
- **MainDispatcherRule** (custom, in `app/src/test/`) to swap `Dispatchers.Main` during tests

Run the full suite locally:

```bash
./gradlew test
```

Run a single module:

```bash
./gradlew :domain:test
./gradlew :usecases:test
```

---

## Getting Started

### Prerequisites

- **JDK 17+** (required by Android Gradle Plugin 9.x)
- **Android Studio Ladybug (2024.2)** or newer
- **Android SDK 36** installed via SDK Manager
- A Firebase project with `google-services.json` (Firestore, Realtime Database, Auth, Analytics and Crashlytics enabled)
- An AdMob account for ad unit IDs (test IDs work out of the box for debug builds)

### Setup

1. Clone the repository:

   ```bash
   git clone https://github.com/AlvaroQ/AdivinaBandera.git
   ```

2. Drop your `google-services.json` in `app/`.

3. Create `app/secrets/secrets.xml` with your AdMob keys. The debug build uses Google's official test IDs, so test values are enough to get it running:

   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <resources>
       <string name="admob_id">ca-app-pub-3940256099942544~3347511713</string>
       <string name="admob_banner_test_id">ca-app-pub-3940256099942544/6300978111</string>
       <string name="admob_bonificado_test_id">ca-app-pub-3940256099942544/5224354917</string>
       <string name="admob_banner_game">ca-app-pub-3940256099942544/6300978111</string>
       <string name="admob_banner_ranking">ca-app-pub-3940256099942544/6300978111</string>
       <string name="admob_bonificado_game">ca-app-pub-3940256099942544/5224354917</string>
   </resources>
   ```

4. Build the debug APK:

   ```bash
   ./gradlew assembleDebug
   ```

5. (Optional) Run the unit tests:

   ```bash
   ./gradlew test
   ```

---

## Roadmap

Work that is on the table but not yet shipped:

- **Accessibility**: high-contrast and large-text modes (PrideQuiz already has this — planned port to AdivinaBandera)
- **Instrumented tests**: Compose UI tests for the critical screens
- **Screenshot tests**: Roborazzi for visual regression across themes
- **Billing**: remove-ads in-app product
- **More languages**: French, Portuguese, Italian and German

---

## Links

- [Play Store listing](https://play.google.com/store/apps/details?id=com.alvaroquintana.adivinabandera) — install AdivinaBandera on your device
- [Report a bug](https://github.com/AlvaroQ/AdivinaBandera/issues/new?labels=bug) — something broken or unexpected
- [Request a feature](https://github.com/AlvaroQ/AdivinaBandera/issues/new?labels=enhancement) — propose an improvement
- [CI workflows](https://github.com/AlvaroQ/AdivinaBandera/actions) — latest build and test runs

---

## License

Released under the [Apache License 2.0](LICENSE). You are free to use, modify, and distribute the code with attribution.
