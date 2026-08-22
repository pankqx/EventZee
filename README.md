# EventZee 🎟️

**EventZee** is an Android app for campus event discovery, registration, and digital e-ticketing — built for students to browse events and for organizers to create and manage them, with QR-code tickets generated on-device.

Built with **Kotlin**, **MVVM architecture**, and **Firebase Firestore** as a real-time cloud backend.

## Features

- **Two roles, one app** — Student and Organizer flows in the same codebase
- **Student side**: browse/search/filter events by category, register for events, pay (UPI flow), view e-tickets with a generated QR code
- **Organizer side**: create events, view registrations per event, track paid vs. pending
- **Real-time data** — powered by Firestore snapshot listeners, so changes sync live across devices
- **On-device QR generation** for e-tickets (ZXing)
- **Session persistence** via SharedPreferences

## Tech Stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| Architecture | MVVM (ViewModel + LiveData + Repository) |
| Backend | Firebase Firestore (cloud, real-time) |
| Async | Kotlin Coroutines |
| UI | ViewBinding, RecyclerView, Fragments |
| QR codes | ZXing |
| Build | Gradle (Kotlin/Groovy DSL), Android Gradle Plugin 8.3.0 |

## Architecture

```
ui/
  auth/        - Splash, Login, Register
  student/     - Events list, Event detail, Registration form, Payment, E-ticket, Profile, My Tickets
  organizer/   - Organizer dashboard, Create event, Registrations list
data/
  model/       - User, Event, Registration (Firestore document models)
  repository/  - AppRepository (Firestore reads/writes) + FirestoreLiveData (real-time helper)
utils/
  SessionManager - lightweight local session (SharedPreferences)
```

Firestore collections: `users`, `events`, `registrations`.

## Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/<your-username>/EventZee.git
   ```
2. **Add your Firebase config.** This repo does **not** include `google-services.json` (it's project-specific and gitignored). To run the app:
   - Create/open a project in the [Firebase console](https://console.firebase.google.com/)
   - Add an Android app with package name `com.eventzee`
   - Download `google-services.json` and place it at `app/google-services.json` (see `app/google-services.json.example` for the expected shape)
   - Enable **Cloud Firestore** in the Firebase console (start in test mode for development)
3. **Open in Android Studio** (Giraffe or newer recommended) and let it sync.
   > This project's checked-in Gradle wrapper binary was regenerated/incomplete at the time of upload — if `./gradlew` doesn't run from the terminal, just open the project directly in Android Studio (it doesn't need the wrapper to import) and use **File → Sync Project with Gradle Files**, or run `gradle wrapper --gradle-version 8.7` once to regenerate it for CLI/CI use.
4. Run on an emulator or device with **minSdk 26+**.

## Known Limitations (honest list)

- **No Firebase Authentication yet** — login is a direct Firestore query matching email/password, and passwords are stored in plain text. Fine for a student project, **not** production-ready.
- **No real payment gateway** — the payment screen shows a UPI ID to pay manually; marking a registration "paid" is just a status flag, not a verified transaction.
- **No automated tests** yet.
- **Open Firestore rules** (`firestore.rules`) for development convenience — must be locked down before any public deployment.

## Roadmap (before Play Store)

- [ ] Firebase Authentication (replace manual password check)
- [ ] Real payment gateway integration (e.g. Razorpay for UPI)
- [ ] Lock down Firestore security rules per-role
- [ ] Unit/instrumentation tests + CI
- [ ] Push notifications for registration/payment confirmation

## Screenshots

*(Add screenshots of the running app here — e.g. Events list, Event detail, E-ticket screen. Repos with visuals get far more attention.)*

## License

MIT — see [LICENSE](LICENSE).
