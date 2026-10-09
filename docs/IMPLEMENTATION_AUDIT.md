# EventZee Implementation Audit

- **Audited commit:** `6e13f82` ("EventZee: migrate data layer from Room to Firebase Firestore"), branch `main`, clean working tree at audit start.
- **Audit date:** 2026-10-09
- **Method:** static reading of every Kotlin source file, the manifest, Gradle files, Firestore rules, and resource inventories (grep counts). **Nothing in this audit was executed.** The Android SDK is not installed in the audit environment, and outbound access to Google Maven, Maven Central, the Gradle plugin portal and the Gradle distribution host is blocked by egress policy (HTTP 403 on CONNECT). So no build, test, emulator or device run has been possible. Every "Working" claim below is therefore **from code reading only** and is labelled accordingly.

## Status legend

| Label | Meaning |
|---|---|
| **Working (by reading)** | Code path is complete and I found no defect; not executed |
| **Partial** | Some of the behaviour exists, key parts missing |
| **Broken** | Code exists but, as written, does not do what its UI implies |
| **Missing** | No implementation |
| **Security** | Security-sensitive finding |
| **Verify** | Needs a device/emulator or Firebase project to confirm |

No feature is marked "Working and verified" because none could be verified.

## 1. Verified technology stack

Confirmed against source and `app/build.gradle`:

| Item | Actual |
|---|---|
| Language / JVM | Kotlin 1.9.0, Java 17 target |
| Build | Gradle (Groovy DSL), AGP 8.3.0, wrapper pinned to Gradle 8.7 |
| SDK levels | minSdk 26, targetSdk/compileSdk 34 |
| UI | XML layouts (26), ViewBinding, Material Components 1.11.0, RecyclerView, Fragments + Activities. **No Jetpack Compose, no Navigation component.** |
| Architecture | MVVM-lite: `AndroidViewModel` + `LiveData`, one `AppRepository`. ViewModels construct the repository themselves (no DI), so nothing is injectable or unit-testable. |
| Backend | Cloud Firestore via Firebase BoM 33.5.1. **`firebase-auth` is not a dependency.** No Cloud Functions, no `firebase.json`, no backend directory. |
| Async | Coroutines 1.7.3 + play-services interop |
| QR | `com.google.zxing:core` 3.5.2 — **encoding only**. No scanner library, no camera permission in the manifest. |
| Tests / CI | None. `testImplementation junit` is declared but `app/src/test` and `app/src/androidTest` do not exist. No `.github/`. |

The previously-circulated description (Kotlin, MVVM, Firestore, coroutines, ViewBinding, RecyclerView, ZXing) is accurate. README's "Gradle (Kotlin/Groovy DSL)" is imprecise: it is Groovy.

**Decision:** keep XML + ViewBinding. No framework migration is justified by anything found.

## 2. Build and repository hygiene

| Item | Status | Detail |
|---|---|---|
| Gradle wrapper | **Broken** | `gradlew` is a 72-byte script that `exec`s `gradle/wrapper/gradle-wrapper.jar`, which is not in the repo (only `gradle-wrapper.properties` is). A CLI build from a clean checkout cannot start. README already admits this. Fix: regenerate with `gradle wrapper --gradle-version 8.7`. |
| Clean command-line build | **Verify** | Cannot be attempted here (blocked hosts above). |
| `google-services.json` | Working (by reading) | Correctly gitignored; `.example` file is placeholder-only. No secrets found in the repo. |
| Release build | Partial | `minifyEnabled false`, no signing config, `allowBackup="true"` (see §6). |
| `.gitignore` | Working | Covers build output, keystores, `local.properties`, Firebase config. |
| Git author on history | Note | The single existing commit is authored by placeholder `EventZee Dev <you@example.com>`. |

## 3. Feature classification

### Authentication and accounts

| Feature | Status | Evidence |
|---|---|---|
| Register (student / organizer) | **Partial + Security** | `AuthViewModel.register` only checks non-empty. No email format check, no password rules, **no duplicate-email check**, role chosen freely by the registrant (anyone can self-register as `organizer`). |
| Login | **Security** | `AppRepository.login` queries `users` where `email == x AND password == y`. Password is stored and compared in plaintext, client-side. |
| Role dropdown on login | **Broken** | `LoginFragment` requires the user to pick a role, but the choice is never used; routing uses `user.role` from Firestore. The control is decorative. |
| Session | **Security** | `SessionManager` keeps `user_id`, role, name and email in SharedPreferences. There is no auth token, and Firestore requests are not authenticated, so identity is purely a client-held string. Logged-in state survives only by that flag. |
| Logout | **Partial** | Exists in student `ProfileFragment`. **No logout handler anywhere for organizers** (`grep clearSession` finds one call site). Organizer layout has no logout control id either. |
| Edit profile | **Broken (fake button)** | `ProfileFragment` shows a "coming soon" toast. |
| Change password | **Broken (fake button)** | Same. `AppRepository.updateUser` exists but is unused by any screen. |
| Error handling on auth | **Broken** | Repository calls in `viewModelScope.launch` have no `try/catch`. An offline/permission failure throws inside the coroutine and **crashes the app**. Only "invalid credentials" produces a message. |

### Student experience

| Feature | Status | Evidence |
|---|---|---|
| Event list (live) | **Partial** | Real-time via snapshot listener. **Drafts are not filtered out** (`getAllEvents` has no status filter). No loading, empty or error state; listener errors are swallowed (`if (error != null) return`). Cards show title/date/venue only — no artwork, organizer, or registration status. |
| Search | Working (by reading) | Client-side contains-match on title, venue, date string. |
| Category filter | **Partial** | Chips exist only for All / Academic / Social / Sports, but organizers can create **Cultural** and **Technical** events that cannot be filtered to. |
| Event detail | **Partial** | Single fetch (not live). `tvVenueSub` is hard-coded "Main Campus". Registration fee, deadline, capacity, agenda, FAQs and organizer details are not shown. No indication if the user already registered. |
| Registration form | **Partial + Security** | Fixed fields (name, email, phone, USN, department, team name). Department list is hard-coded to six engineering branches. USN is mandatory for everyone. Team name is never required even when `maxTeamPlayers > 1`. No email/phone format validation. Not data-driven — this is the "hardcoded screen" the product vision rejects. |
| Duplicate-registration protection | **Missing** | Nothing prevents the same user registering for the same event repeatedly, and the submit button is not disabled during the network call, so a double-tap creates two registrations. |
| Capacity / waitlist | **Missing** | `Event` has no capacity field. |
| Registration deadline | **Missing** | No field. |
| Payment | **Broken + Security** | `PaymentActivity` shows a UPI ID; tapping "submit" calls `updatePaymentStatus(reg, "PAID")` with **no verification of any kind**. Any user can mark themselves paid, and Firestore rules would allow anyone to write `PAID` directly. The write is launched in the Activity's `viewModelScope` and the Activity then calls `finish()`, so the ViewModel is cleared and the coroutine can be cancelled mid-flight (read-then-write of the registration) — payment may silently not be recorded. **Verify** on device. |
| Post-payment navigation | **Broken** | `PaymentActivity` passes `open_tab=tickets`; **no code reads that extra**. The user lands on the Events tab. |
| My Tickets list | **Partial** | `TicketAdapter` starts an unscoped `CoroutineScope(Dispatchers.Main).launch` per bound row and does one Firestore `get` per row (N+1). Not lifecycle-safe; recycled rows can display another row's event title. The `getEvent: (String) -> AppRepository` parameter is a leftover workaround. No upcoming/past split. |
| E-ticket + QR | **Partial + Security** | QR is generated on-device (ZXing) and encodes only the raw `ticketCode`. `ETicketActivity` **does not check `paymentStatus`** — any registration id opens a ticket and QR, including unpaid ones. No attendee name on the ticket. Ticket code is `EZ-<1000..9999>-X<100..999>` from `kotlin.random` on the client: ~8.1M values, non-cryptographic, never checked for uniqueness, trivially guessable/forgeable. Failures are `printStackTrace()`d and the user sees a blank QR. |
| Cancel / modify registration | **Missing** | |

### Organizer experience

| Feature | Status | Evidence |
|---|---|---|
| Dashboard / my events | **Partial** | Lists the organizer's events via live query. No metrics, trends, pending approvals, loading or empty states. |
| Create event | **Partial** | Required: title, date, time, venue. Always saved as `status = "Live"`. No validation of date being in the future. Date is stored as a US-format **string** (`M/d/yyyy`) and time as a string → events cannot be reliably sorted by date, filtered as "upcoming", or timezone-handled. `registrationFee` is **not editable** in the UI, so every event silently carries the model default **₹299**. `createdAt` uses the client clock. Button not disabled during save (double-tap creates duplicates). |
| Draft events | **Missing in practice** | The model supports `"Draft"` but no screen can create one. |
| Edit event | **Broken** | The "Edit" button (shown only for Draft events, which cannot exist) opens `CreateEventActivity` with an `event_id` extra that the Activity **never reads**; it would create a new event. `AppRepository.updateEvent` is unused. |
| Publish / close / archive | **Missing** | |
| Registrations list | **Partial + Security** | Live list with search by name/USN. No sort, filter, detail view, approval, or export. **No ownership check**: any signed-in user who has an event id can open it, and Firestore rules allow reads of every registration (including phone/email/USN) by anyone, authenticated or not. |
| "Earnings" figure | **Broken (fabricated number)** | `RegistrationsListActivity` computes `paid * 200` with a comment "demo calculation". It ignores the event's real fee. Violates the "no fake metrics" requirement. |
| Attendance / check-in | **Missing** | No scanner, no camera permission, no attendance record. |
| CSV export | **Missing** | |
| Analytics | **Missing** | |

### Platform features from the product vision

| Capability | Status |
|---|---|
| Reusable, versioned form builder | **Missing** (registration fields are hard-coded in one Activity + one data class) |
| Public web registration / shareable link / QR link | **Missing** (no web client; no deep links; manifest has no intent filters) |
| Approvals, capacity, waitlist | **Missing** |
| Attendee communication | **Missing** |
| Reports / analytics | **Missing** |

## 4. Firestore data model (as it exists)

Three top-level collections, no subcollections, no indexes file:

- `users` — `{name, email, password, role, usn, department, phone, createdAt}`; document id is Firestore auto-id (not an Auth uid).
- `events` — `{title, institution, organizerName, date, time, venue, category, description, maxTeamPlayers, website, coordinators, upiId, registrationFee, status, organizerUserId, createdAt}`.
- `registrations` — `{eventId, userId, studentName, studentEmail, studentPhone, usn, department, teamName, paymentStatus, ticketCode, seatInfo, createdAt}`.

Observations:
- Personal data (name/email/phone/USN) is **denormalised into each registration** with no purpose limitation, retention policy or deletion path.
- `registrations` has no unique key on `(eventId, userId)`, so uniqueness cannot be enforced; counting is done by client queries (`getPaidCount` is unused).
- `ticketCode` is stored in the same document the organizer and any other client can read, so a ticket can be copied by anyone who can list registrations.
- `updateRegistration` / `updateEvent` use full-document `set()`, so concurrent edits overwrite each other (lost-update risk).
- Several queries (`whereEqualTo` + `orderBy`) will need composite indexes once ordering is added; none are declared.

## 5. Security findings (priority order)

| # | Severity | Finding |
|---|---|---|
| S1 | **Critical** | `firestore.rules` is `allow read, write: if true` for every document. Anyone on the internet with the project's public config can read, modify, or delete all users, events and registrations. |
| S2 | **Critical** | Passwords stored in plaintext in `users`, and readable by anyone via S1. Login is a client-side equality query. |
| S3 | **Critical** | Payment confirmation is a client-set string with no verification; ticket access does not depend on it either (§3 E-ticket). |
| S4 | High | No authentication identity exists server-side, so ownership ("only the organizer of event X sees its attendees") cannot be enforced even if rules were written. Requires moving to Firebase Authentication first. |
| S5 | High | Role escalation: self-selected `organizer` role at sign-up, stored in a world-writable collection. |
| S6 | High | Tickets are forgeable/guessable (client `kotlin.random`, no signature, no server-side validation, no replay/duplicate-scan protection — and no scanner at all). |
| S7 | Medium | No capacity or duplicate checks and no transactions; concurrent registrations cannot be made consistent client-side. |
| S8 | Medium | `allowBackup="true"` lets session data and cached Firestore data be included in device backups. |
| S9 | Medium | Unhandled coroutine exceptions crash the app (availability) and `printStackTrace()` is used for error reporting (inconsistent, may log data). |
| S10 | Low | Release build is not minified/obfuscated; no signing config documented. |

**Required remediation order** (also reflected in the plan): Firebase Authentication → rules written against `request.auth.uid` and a server-set role → server-side (Cloud Functions or rule-constrained transactions) for registration creation, ticket issuance and check-in → verified payments via a provider webhook. Rules cannot be meaningfully tightened *before* Authentication exists, because today no request carries an identity.

## 6. UI / design / accessibility inventory

Counts from `app/src/main/res`:

- 26 layouts, 58 drawables, **1 string** in `strings.xml` (`app_name`). **162 hard-coded `android:text` literals** in layouts → no localisation, no consistent copy management.
- `colors.xml` defines 20 colours (navy `#0D1B2A` primary, orange accent `#E8702A`), but **63 hex literals are hard-coded inside drawables** and 2 inside layouts, so the palette is not actually centralised.
- **0 `contentDescription` attributes across 67 `ImageView`s** → icons and images are invisible to TalkBack. Interactive text-with-icon rows ("Edit profile", "Copy UPI") are plain `TextView`/`LinearLayout` click targets; touch-target sizes not yet measured (**Verify**).
- Typography is the platform default; no font resources, no type scale. Text sizes are all `sp` (good).
- No `values-night`, no dimens file / spacing scale, no styles file, no shared component styles (buttons are separate drawables: `bg_button_primary`, `btn_primary`, `bg_btn_outline`, `btn_outline`, … — several appear to be near-duplicates).
- Screens use Toasts for validation errors; there is no field-level error presentation.
- Splash is a fixed 2-second `Handler.postDelayed`.
- No loading skeletons, empty states (only a bare `tvEmpty` on My Tickets), or network-error states on any screen.
- Event "artwork": none; `ItemEvent` has no image view usage in the adapter.

## 7. What is genuinely solid

To avoid under-selling the base:
- Clear package structure (`ui/{auth,student,organizer}`, `data/{model,repository}`, `utils`).
- Real-time Firestore listeners wrapped in lifecycle-aware `LiveData` (attached in `onActive`, removed in `onInactive`).
- `DiffUtil`-based `ListAdapter`s throughout; ViewBinding with correct `_binding = null` handling in fragments.
- A working on-device QR encoder.
- Honest README known-limitations list, which this audit confirms.
- Sensible secret hygiene for `google-services.json`.

## 8. Items that need a device, emulator, or Firebase project to verify

1. App cold start, splash, login → role routing.
2. Whether `PaymentActivity`'s write completes after `finish()` (suspected race, §3).
3. `TicketAdapter` row-recycling showing wrong event titles under scrolling.
4. Touch-target sizes, contrast ratios and font-scaling behaviour.
5. Composite-index requirements surfaced by real queries.
6. Offline behaviour of every Firestore call.

## 9. Environment limits affecting this audit and later stages

Recorded here because they bound what can honestly be claimed:

- **No Android SDK; build/test hosts blocked.** `dl.google.com`, `maven.google.com`, `repo.maven.apache.org`, `plugins.gradle.org` and `services.gradle.org` all returned 403 from the egress proxy (policy denial; not retried or bypassed). Android compilation and instrumented tests cannot be run in this environment. `registry.npmjs.org` is reachable, so a JavaScript/TypeScript web client and its tests *can* be run here.
- **Git push credential invalid.** `GH_TOKEN` is rejected by GitHub (`gh auth status`).
- **Git identity.** The identity configured in this environment is `Claude <noreply@anthropic.com>`. The project owner's stated rule is that commits carry the owner's own identity as sole author with no co-author trailers, so no commits have been made.
- **No Firebase project / credentials** are available, so Firestore rules cannot be exercised against an emulator or live project from here.
