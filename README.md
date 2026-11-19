# Jordan Tourist Guide · دليل الأردن السياحي

A native Android app that helps tourists discover Jordan: browse famous places, search and filter them, see them on a map, save favorites offline and share reviews. The app is fully bilingual (English / Arabic, with right-to-left layout).

> Zarqa University · Faculty of Information Technology · Computer Science, team project

| Team member | Student ID | Role |
|---|---|---|
| **Mahmoud Ghunaim** | 202300089 | Team leader |
| Ahmed Abu Rabie | 202303206 | Member |
| Khalid Jawlaj | 202301219 | Member |

---

## 1. Project idea

Visitors to Jordan often have to search many websites to find out where a place is, when it opens and how much the ticket costs. **Jordan Tourist Guide** collects this in one app, in Arabic and English:

- 19 real places (Petra, Wadi Rum, Jerash, the Dead Sea, Aqaba, castles, religious sites, nature reserves and restaurants), each with a description, opening hours, ticket prices for Jordanians and foreigners, a rating and photos.
- A map of all places with one-tap directions in Google Maps.
- Favorites that work **without internet**.
- Reviews from registered travellers.

## 2. Features

| # | Screen | What it does |
|---|---|---|
| 1 | **Splash / Onboarding** | Branded splash, then 3 intro pages shown **only on first launch** (saved in DataStore). |
| 2 | **Login / Register** | Email and password authentication (Firebase Auth, or offline demo accounts). "Continue as guest". |
| 3 | **Home** | Greeting, search bar, category chips, "Top rated" carousel, "All places" grid. |
| 4 | **Search and filter** | Search by name (Arabic or English) and filter by category and governorate. |
| 5 | **Place details** | Image pager, description, opening hours, ticket prices, rating, "Open in Google Maps", add/remove favorite, reviews list. |
| 6 | **Add review** | Star rating and comment. Only for logged-in users; guests are asked to log in. |
| 7 | **Map** | All places as colored markers (one color per category). Tap a marker to see a small card, then open the details. |
| 8 | **Favorites** | Saved in a Room database and works offline. |
| 9 | **Profile / Settings** | User info, log out, language switch (العربية / English), theme (System / Light / Dark), team info. |

Every screen has **loading**, **empty** and **error** states, and every error state has a "Try again" button.

**Design:** a Material 3 theme inspired by Jordan: **Petra rose-red** (primary), **Dead Sea blue** (secondary), **Wadi Rum gold** (accent) and **desert sand** backgrounds, with a matching dark theme.

## 3. Tech stack

| Area | Library |
|---|---|
| Language / UI | Kotlin, Jetpack Compose, Material 3 |
| Navigation | Navigation Compose (type-safe routes), single activity |
| Architecture | MVVM, `StateFlow` UI state, repository pattern |
| Dependency injection | Hilt |
| Local database | Room (favorites, offline reviews, demo accounts) |
| Settings | DataStore Preferences (onboarding flag, theme) |
| Backend | Firebase Authentication and Cloud Firestore (optional) |
| Maps | Google Maps Compose (`maps-compose`) |
| Images | Coil |
| JSON | kotlinx.serialization |
| Tests | JUnit 4, kotlinx-coroutines-test, hand-written fakes |
| Build | Gradle Kotlin DSL, version catalog (`gradle/libs.versions.toml`), AGP 9 |

`minSdk 26` (Android 8.0) · `targetSdk 36` · `compileSdk 36`

## 4. Architecture

The app follows **MVVM** with four layers. Each layer only talks to the layer directly below it.

```
┌──────────────────────────────────────────────────────────────────────┐
│  UI layer (Jetpack Compose)                         ui/              │
│  Screens observe state and send user events to their ViewModel       │
│  HomeScreen · SearchScreen · PlaceDetailsScreen · MapScreen · …      │
└───────────────▲───────────────────────────────┬──────────────────────┘
                │ StateFlow<UiState>            │ events (onClick, …)
┌───────────────┴───────────────────────────────▼──────────────────────┐
│  ViewModel layer                                    viewmodel/       │
│  Holds screen state, validation, combines data from repositories    │
│  HomeViewModel · SearchViewModel · PlaceDetailsViewModel · …         │
└───────────────▲───────────────────────────────┬──────────────────────┘
                │ suspend fun / Flow            │
┌───────────────┴───────────────────────────────▼──────────────────────┐
│  Repository layer (interfaces)                      repository/      │
│  PlaceRepository · ReviewRepository · AuthRepository · Favorites…    │
│        │ chosen by di/RepositoryModule using AppConfig.USE_FIREBASE  │
│   ┌────┴──────────────────────┐        ┌──────────────────────────┐  │
│   │ Local*Repository (default)│        │ Firestore*/Firebase*Repo │  │
│   └────┬──────────────────────┘        └───────────┬──────────────┘  │
└────────┼───────────────────────────────────────────┼─────────────────┘
┌────────▼───────────────────────────────────────────▼─────────────────┐
│  Data layer                                         data/            │
│  assets/places.json · Room (AppDatabase, DAOs) · DataStore           │
│  Firebase Auth · Cloud Firestore            models: Place, Review, … │
└──────────────────────────────────────────────────────────────────────┘
          Hilt (di/) creates every object and injects it where needed.
```

**Data flow example (opening a place):**
`PlaceDetailsScreen` → `PlaceDetailsViewModel` reads `placeId` from navigation → calls `PlaceRepository.getPlace(id)` and observes `FavoritesRepository.observeIsFavorite(id)` and `ReviewRepository.observeReviews(id)` → combines them into one `PlaceDetailsUiState` (a `StateFlow`) → the screen redraws automatically when anything changes, for example after tapping the heart.

### Local vs Firebase (one flag)

```kotlin
// app/src/main/java/edu/zu/jordantouristguide/config/AppConfig.kt
object AppConfig {
    const val USE_FIREBASE = false   // ← flip to true after adding google-services.json
}
```

| | `USE_FIREBASE = false` (default) | `USE_FIREBASE = true` |
|---|---|---|
| Places | `LocalPlaceRepository` reads `assets/places.json` | `FirestorePlaceRepository` (`places` collection) |
| Reviews | `LocalReviewRepository` (Room) | `FirestoreReviewRepository` (`reviews` collection, real time) |
| Login | `LocalAuthRepository` (demo accounts in Room, SHA-256 hashed passwords) | `FirebaseAuthRepository` (email and password) |
| Favorites | Room (both modes, always offline) | Room |

Safety net: Firebase is used only if the flag is `true` **and** `app/google-services.json` existed at build time (`BuildConfig.FIREBASE_CONFIGURED`), so a missing config file can never crash the app.

## 5. Folder structure

```
JordanTouristGuide/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── assets/places.json              ← seed data (19 places, AR + EN)
│       │   ├── res/values/strings.xml          ← English strings
│       │   ├── res/values-ar/strings.xml       ← Arabic strings
│       │   └── java/edu/zu/jordantouristguide/
│       │       ├── JordanGuideApp.kt           ← @HiltAndroidApp
│       │       ├── MainActivity.kt             ← the single activity
│       │       ├── config/AppConfig.kt         ← local / Firebase switch
│       │       ├── data/
│       │       │   ├── model/                  ← Place, Review, User, Category, Governorate, PlaceFilter …
│       │       │   ├── local/                  ← Room: AppDatabase, DAOs, entities; AssetReader
│       │       │   └── preferences/            ← DataStore settings
│       │       ├── repository/                 ← interfaces + Local / Firestore implementations
│       │       ├── di/                         ← Hilt modules (DataModule, RepositoryModule)
│       │       ├── viewmodel/                  ← one ViewModel per screen + UiState
│       │       └── ui/
│       │           ├── theme/                  ← colors, typography, light/dark theme
│       │           ├── navigation/             ← routes, NavHost, bottom bar
│       │           ├── components/             ← cards, rating stars, chips, loading/empty/error views
│       │           ├── screens/                ← splash, onboarding, auth, home, search, details,
│       │           │                              review, map, favorites, profile
│       │           └── util/                   ← labels, maps intent, system bars
│       └── test/java/edu/zu/jordantouristguide/
│           ├── viewmodel/                      ← ViewModel unit tests
│           ├── repository/                     ← repository unit tests
│           ├── data/model/                     ← filter logic tests
│           └── testutil/                       ← fakes, MainDispatcherRule
├── gradle/libs.versions.toml                   ← all library versions in one place
├── firestore.rules                             ← Firestore security rules
└── README.md
```

## 6. How to run

**Requirements:** Android Studio (latest stable) or IntelliJ IDEA with the Android plugin, and the Android SDK with API 36 installed. You don't need to install a JDK yourself: Gradle downloads JDK 17 automatically the first time (Foojay toolchain resolver).

1. Open the `JordanTouristGuide` folder in Android Studio and wait for Gradle sync.
2. Choose an emulator or a phone (Android 8.0+) and press **Run ▶**.

From the command line:

```bash
./gradlew assembleDebug          # build the APK → app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest      # run the unit tests
./gradlew installDebug           # install on a connected device / emulator
```

The app runs straight away in **offline mode** with no Firebase and no API keys. In this mode you can register a demo account on the login screen; it is stored on the device.

### Google Maps API key (for the Map screen)

Without a key, the Map tab shows a list of all places instead of the map. To enable the real map:

1. Go to [Google Cloud Console](https://console.cloud.google.com/) → create or select a project → **APIs & Services → Library** → enable **Maps SDK for Android**.
2. **APIs & Services → Credentials → Create credentials → API key**. Restrict it to Android apps with package `edu.zu.jordantouristguide` and your debug SHA-1 (`./gradlew signingReport`).
3. Add it to `local.properties` in the project root (this file is not committed to git):
   ```properties
   MAPS_API_KEY=AIza...your-key...
   ```
4. Rebuild the app.

## 7. How to connect Firebase

> **Status:** the app is already connected to the Firebase project **`jordan-tourist-guide-zu`**
> (`AppConfig.USE_FIREBASE = true`). Auth (Email/Password) is enabled, Firestore is created,
> the 19 places are uploaded and the rules in `firestore.rules` are deployed.
>
> **Teammates:** `app/google-services.json` is not committed. Get it from the team leader, or,
> if you were added to the Firebase project, run:
> `firebase apps:sdkconfig ANDROID --project jordan-tourist-guide-zu -o app/google-services.json`.
> Without the file the app still builds and simply runs in offline mode.
>
> Deploy rule changes with `firebase deploy --only firestore:rules`.

The steps below are how it was set up (repeat them for a new Firebase project):

1. **Create a project** at [console.firebase.google.com](https://console.firebase.google.com/).
2. **Add an Android app** with package name **`edu.zu.jordantouristguide`**. Optionally add the debug SHA-1 from `./gradlew signingReport`.
3. **Download `google-services.json`** and put it in the **`app/`** folder (next to `app/build.gradle.kts`).
   The build applies the `com.google.gms.google-services` plugin automatically when this file exists.
4. **Authentication** → *Sign-in method* → enable **Email/Password**.
5. **Firestore Database** → *Create database* (choose a location near Jordan, e.g. `europe-west`).
6. **Rules:** copy the contents of [`firestore.rules`](firestore.rules) into *Firestore → Rules*.
   For the **first launch only**, change the `places` rule to `allow write: if true;` so the app can upload the seed data.
7. Open `config/AppConfig.kt` and set:
   ```kotlin
   const val USE_FIREBASE = true
   ```
8. Run the app. When the `places` collection is empty, the app uploads all places from `assets/places.json` once (`AppConfig.SEED_FIRESTORE_IF_EMPTY`). Then set the `places` rule back to `allow write: if false;`.

The Profile screen shows the current **data source** ("Offline" or "Firebase"), so you can check which mode is active.

**Firestore data model**

```
places/{placeId}
  nameAr, nameEn, descriptionAr, descriptionEn : string
  category    : "Historical" | "Nature" | "Religious" | "Entertainment" | "Restaurants"
  governorate : "Amman" | "Irbid" | "Zarqa" | "Balqa" | "Madaba" | "Mafraq" | "Jerash"
                | "Ajloun" | "Karak" | "Tafilah" | "Maan" | "Aqaba"
  latitude, longitude, rating : number
  openingHours : string
  ticketPrice  : { local: number, foreign: number | null }   // JOD; null = same as local
  imageUrls    : array<string>

reviews/{reviewId}
  placeId, userId, userName, comment : string
  rating    : number (1–5)
  timestamp : number (epoch millis)
```

## 8. Testing

Unit tests run on the plain JVM (no emulator needed). They use **fake repositories** instead of mocks, so they are easy to read.

| Test class | What it checks |
|---|---|
| `LocalPlaceRepositoryTest` | JSON parsing, lookup by id, caching, invalid JSON, and that the real `places.json` is valid (≥ 15 places, unique ids, coordinates inside Jordan, every category present) |
| `LocalAuthRepositoryTest` | register, login, wrong password, duplicate email, weak password |
| `PlaceFilterTest` | search in Arabic and English, category and governorate filters, top-rated sorting |
| `HomeViewModelTest` | loading places, top rated, category chips, error state and retry, greeting |
| `SearchViewModelTest` | search, filters, category passed from navigation, reset |
| `PlaceDetailsViewModelTest` | loading the place, not found, network error, toggling favorites, reviews, login state |
| `AddReviewViewModelTest` | guests blocked, validation, saving, save error, comment length limit |
| `AuthViewModelTest` | email and password validation, login, register, error messages |
| `FavoritesViewModelTest`, `MapViewModelTest`, `SplashViewModelTest`, `ProfileViewModelTest` | favorites list, map selection, start destination, theme and logout |

```bash
./gradlew testDebugUnitTest
# report: app/build/reports/tests/testDebugUnitTest/index.html
```

## 9. Notes

- **Ticket prices and opening hours** are approximate and are for demonstration only. Check official sources (e.g. visitjordan.com) before travelling.
- **Images** are placeholder photos from `picsum.photos`. To use real photos, replace the `imageUrls` in `places.json` (or in Firestore) with your own image links.
- The **language switch** uses AndroidX per-app languages (`AppCompatDelegate.setApplicationLocales`), so it also works on Android 8 to 12. On Android 13 and later the choice also appears in the system settings under *Apps → Jordan Tourist Guide → Language*.
