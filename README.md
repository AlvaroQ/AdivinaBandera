# AdivinaBandera

<p align="center">
  <img src="capture/game.gif" width="250" alt="AdivinaBandera gameplay"> 
</p>
 
AdivinaBandera is an Android game to learn and practice world flags, built as a real production project with modular Clean Architecture and MVVM.

## Features

- Flag quiz gameplay with score tracking.
- Modular architecture focused on maintainability and testability.
- Dependency injection with Koin.
- Firebase integration: Analytics, Crashlytics, Firestore, Realtime Database, and Auth.
- Monetization with Google AdMob and consent support (UMP).

## Architecture

The project follows Clean Architecture principles inspired by Robert C. Martin, with clear module boundaries:

- `app`: Android UI and presentation layer (MVVM + Jetpack Compose).
- `usecases`: Application use cases and orchestration.
- `domain`: Business entities and contracts.
- `data`: Repositories, persistence, and remote/local data sources.

## Tech Stack

- Kotlin + Coroutines
- Jetpack Compose + Navigation
- Koin (DI)
- Room + DataStore + WorkManager
- Firebase (Analytics, Crashlytics, Firestore, Realtime Database, Auth)
- Google AdMob

## Google Play

https://play.google.com/store/apps/details?id=com.alvaroquintana.adivinabandera

## Screenshots

<p align="center">
 <img src="capture/es/image1.jpg" width="250" alt="AdivinaBandera"> 
 <img src="capture/es/image2.jpg" width="250" alt="AdivinaBandera"> 
 <img src="capture/es/image3.jpg" width="250" alt="AdivinaBandera"> 
 <img src="capture/es/image4.jpg" width="250" alt="AdivinaBandera"> 
 <img src="capture/es/image5.jpg" width="250" alt="AdivinaBandera"> 
</p>
