# EcoShare - Native Android Application

EcoShare is a 100% native Android application built in Kotlin for community resource sharing and neighborhood sustainability. It allows residents to share, discover, bookmark, coordinate, and chat in real-time to exchange tools, food, books, appliances, and more, while tracking community CO2 offsets and sustainability ranks.

---

## 📱 Native Architecture

The application is built using modern Android Jetpack architecture components:
- **Language**: Kotlin 2.0+
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **UI & Layouts**: Native XML layouts with ViewBinding and Material Design Components 3
- **Asynchronous Flow**: Kotlin Coroutines & `StateFlow` / `callbackFlow`
- **Networking & Backend**: Native Firebase SDK (Auth, Cloud Firestore, Firebase Storage, Firebase Cloud Messaging)
- **Image Loading**: Glide
- **Device Sensors**: Play Services Location (GPS coordinates for nearby items) & CameraX / ActivityResultContracts for photos
- **Push Notifications**: Firebase Cloud Messaging (`FirebaseMessagingService`)

```
Native Android UI (Activities / Fragments)
            ↓
       ViewModels
            ↓
       Repositories
            ↓
   Google Firebase / Android APIs
```

---

## 🌟 Core Native Features

1. **Authentication & Session Persistence**:
   - Native Firebase Email & Password authentication
   - Registration with community neighborhood location
   - Password reset via Firebase Auth
   - Automatic session validation and routing via `SplashActivity`
   - Single-active-session listener via Firestore user document observer

2. **Resource Catalog & Filtering**:
   - Real-time updates from Cloud Firestore `resources` collection
   - Interactive category chip selector (`All`, `Tools`, `Food`, `Books`, `Appliances`, `Garden`, `Clothing`, `Furniture`)
   - Tabbed filtering: **Recent**, **Nearby** (Haversine GPS distance calculation), **Saved** bookmarks, **My Shares**
   - Instant search across title, description, and location
   - Real-time status indicators: Available, Pending, Shared, Completed

3. **Resource Sharing & Editing**:
   - Native camera and photo picker integration
   - Automated GPS coordinate detection via `FusedLocationProviderClient`
   - Direct Firebase Storage image uploads (`/resources/`)
   - Edit, delete, and mark as completed/shared with full permission checks

4. **Real-time Messaging & Community Lobby**:
   - Real-time 1-on-1 coordination chat threads
   - Community Lobby (`general_lobby`) where all community members can interact
   - In-app message notifications & FCM push notifications

5. **Community Events**:
   - Community swap meets, repair cafes, and zero-waste workshops
   - Real-time RSVP attendee tracking
   - Host new neighborhood events with date/time pickers

6. **Profile & Gamified Sustainability Ranking**:
   - EcoPoints and cumulative CO2 offset tracking (2.5 kg CO2 saved per item)
   - Dynamic tier ranking: 🌱 *Eco Seedling* → 🌿 *Green Sprout* → 🌳 *Forest Guardian* → 👑 *Zero Waste Legend*
   - Milestone achievement badges
   - Edit display name, community neighborhood, and avatar photo

---

## 🛠️ Project Structure

```
EcoShare/
├── android/
│   ├── app/
│   │   ├── src/
│   │   │   └── main/
│   │   │       ├── java/com/ecoshare/app/
│   │   │       │   ├── data/          # Firebase repositories (Auth, Resource, Chat, Event, Storage)
│   │   │       │   ├── model/         # Native Kotlin data models (User, Resource, Chat, Message, Event)
│   │   │       │   ├── service/       # Firebase Cloud Messaging service (FCM)
│   │   │       │   └── ui/            # Native activities, fragments, viewmodels, and adapters
│   │   │       ├── res/               # Material XML layouts, vectors, drawables, styles, colors
│   │   │       └── AndroidManifest.xml
│   │   ├── build.gradle               # App Gradle config, dependencies, Firebase BoM
│   │   └── google-services.json       # App Firebase configuration
│   ├── gradle/
│   ├── gradlew
│   ├── gradlew.bat
│   └── settings.gradle
├── google-services.json               # Root Firebase credentials
├── firebase.json                      # Firebase project configuration
├── firestore.rules                    # Cloud Firestore security rules
└── storage.rules                      # Firebase Storage security rules
```

---

## 🚀 Building and Running

### Prerequisites
- JDK 17 or JDK 21 (Microsoft OpenJDK / Oracle OpenJDK)
- Android SDK Platform 34 (API Level 34) & Build Tools 34.0.0
- Android Studio Ladybug or newer (optional for GUI development)

### Build Debug APK via Command Line
```powershell
cd android
.\gradlew.bat assembleDebug
```
The output APK is generated at:
`android/app/build/outputs/apk/debug/app-debug.apk`

---

## 🔒 Security & Backend Compatibility

The app communicates directly with Cloud Firestore and Firebase Storage using the canonical collections:
- `users`: User profiles, role (`resident`/`admin`), approved status, active session IDs
- `resources`: Community item listings with category, owner, coordinates, and status
- `chats`: Coordination threads with nested `messages` subcollection
- `events`: Neighborhood workshops and swap meets with attendee arrays
