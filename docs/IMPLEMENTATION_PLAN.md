# EventZee 2.0 Implementation Plan

Status values: **not started**, **in progress**, **blocked**, **complete**.
Last updated: 2026-10-09. Baseline: commit `6e13f82`. Findings referenced as §n / Sn are in `IMPLEMENTATION_AUDIT.md`.

## Blockers (apply across stages)

| ID | Blocker | Affects | Needed from owner |
|---|---|---|---|
| B1 | **Git identity.** The environment's configured identity is `Claude <noreply@anthropic.com>`, not the owner's. Commits must be authored and committed solely by the owner's identity, with no co-author trailers. | Every commit | The `user.name` / `user.email` to use for this repo (local config only) |
| B2 | **No push credential.** `GH_TOKEN` is rejected by GitHub. | Pushing, GitHub Actions run results | A valid token or a push route |
| B3 | **Android build/test not possible here.** Android SDK absent; Google Maven, Maven Central, Gradle plugin portal and Gradle distribution hosts are policy-blocked (403). | Any claim that the Android app builds or its tests pass | Run builds locally / in CI, or lift the egress restriction |
| B4 | **No Firebase project.** No `google-services.json`, no emulator download route verified. | Rules testing, Auth, Functions, live demo | A Firebase project (or emulator access) and a config file kept out of Git |
| B5 | **Payment provider** account and keys. | Verified payments | Provider choice (e.g. Razorpay) and test keys, stored as secrets |

Until B1 is resolved nothing is committed; work stays in the working tree.

## Rule for Android work while B3 stands

Kotlin/XML changes cannot be compiled here. Any such change will be labelled **"written, not compiled"** in this plan and in the README feature table, and will not be counted as complete until the CI job (T8.3) or the owner's local build passes. Logic that can be isolated from Android (validation, ticket signing, form schema, CSV escaping, capacity rules) will be written as pure functions so it can be tested in CI without a device, and mirrored in the web client where JavaScript tests can run here.

## Stage 1 — Audit

| Task | Status |
|---|---|
| T1.1 Inspect full repo, stack, manifest, Gradle, rules | complete |
| T1.2 Write `docs/IMPLEMENTATION_AUDIT.md` | complete |
| T1.3 Verify build from clean checkout | **blocked (B3)** |
| T1.4 Regenerate Gradle wrapper (`gradle wrapper --gradle-version 8.7`) and commit jar | blocked (B1, B3) |

## Stage 2 — Design foundation

| Task | Status |
|---|---|
| T2.1 Define tokens in `values/` : colours (ink / ivory / lime accent / neutrals / semantic), type scale, spacing, radii, elevation, dimens (48dp touch targets) | not started |
| T2.2 Replace the 63 hard-coded hex values in drawables with token references | not started |
| T2.3 Choose and bundle licensed fonts (e.g. OFL display + text pair) as font resources | not started |
| T2.4 Shared styles: button variants, text fields with error state, chips, badges, cards, dialogs | not started |
| T2.5 Move 162 layout string literals to `strings.xml` | not started |
| T2.6 Add `contentDescription` to the 67 ImageViews (or mark decorative) | not started |
| T2.7 Write `docs/DESIGN_SYSTEM.md` | not started |

## Stage 3 — Security foundation (moved ahead of visual redesign)

Rationale: today no request carries identity (§5, S4), so no later security work is meaningful until Authentication exists. This stage is therefore the true prerequisite for the registration engine.

| Task | Status |
|---|---|
| T3.1 Add Firebase Authentication (email/password); remove `password` from `User`; stop storing plaintext | not started (B4) |
| T3.2 Key `users/{uid}` by Auth uid; migrate role to a server-controlled field (custom claim or admin-written doc) so users cannot self-grant `organizer` | not started (B4) |
| T3.3 Rewrite `firestore.rules`: deny by default; owner-only organizer reads of their event's registrations; students read only own registrations; no client writes to `paymentStatus`, `ticketCode`, `checkedInAt` | not started |
| T3.4 Rules unit tests (`@firebase/rules-unit-testing`) covering each denied/allowed case | not started (B4: emulator availability to be verified) |
| T3.5 Try/catch and error states around every repository call (fixes crash on failure, S9) | not started |
| T3.6 Set `allowBackup=false` / data-extraction rules (S8) | not started |

## Stage 4 — Core visual redesign

| Task | Status |
|---|---|
| T4.1 Events discovery: artwork cards, featured, filters (all categories), loading/empty/error states, hide drafts | not started |
| T4.2 Event detail: hero, agenda, deadline, capacity, one primary action, "already registered" state | not started |
| T4.3 Store dates as `Timestamp` (start/end/registrationDeadline) instead of strings; migrate existing docs | not started |
| T4.4 Organizer dashboard with real metrics only; remove fabricated `paid * 200` | not started |
| T4.5 Event create/edit/publish/close/archive with real edit path and drafts; fee field | not started |
| T4.6 Fix post-payment navigation (`open_tab`), organizer logout, remove or implement "coming soon" profile rows | not started |
| T4.7 Disable submit buttons while in flight everywhere | not started |

## Stage 5 — Registration engine (vertical slice)

First end-to-end slice requested by the brief: *create an event → publish its form → submit a registration → view the attendee record.*

| Task | Status |
|---|---|
| T5.1 Form schema v1 (JSON, versioned, question types in brief §6) and a pure validator, with unit tests | not started |
| T5.2 Persist `forms/{formId}` with `version`; each submission stores `formVersion` plus an immutable snapshot of question labels | not started |
| T5.3 Android form renderer driven by schema (replaces `RegistrationFormActivity` hard-coding) | not started — "written, not compiled" until B3 clears |
| T5.4 Server-side registration creation (callable function or rule-constrained transaction): enforces deadline, capacity, one registration per (event, user), generates ticket id | not started (B4) |
| T5.5 Organizer response view and form builder (web client first; mobile builder deferred) | not started |
| T5.6 Conditional logic | not started (deliberately after T5.1–T5.5 are reliable) |

## Stage 6 — Event operations

| Task | Status |
|---|---|
| T6.1 Ticket identity: unguessable id plus server signature; QR carries id + signature, never trusted alone | not started |
| T6.2 Check-in function: states valid / invalid / duplicate / already-used / wrong-event, with timestamp and scanner uid; transactional | not started |
| T6.3 Scanner UI with camera permission and failure handling (adds a scan library — dependency change to be documented) | not started — "written, not compiled" until B3 clears |
| T6.4 Attendee management: search, filter, sort, approve/reject, bulk actions | not started |
| T6.5 CSV export with formula-injection escaping and tests | not started |
| T6.6 Payments: provider webhook marks `PAID`; client can never set it | blocked (B5) |

## Stage 7 — Web client (verifiable in this environment)

Stack to be chosen in the Stage 5 design note; constraint: runs and tests with Node available here, reuses the same Firebase backend, no separate server.

| Task | Status |
|---|---|
| T7.1 Public event page + registration page rendered from form schema | not started |
| T7.2 Shareable link + QR code for an event | not started |
| T7.3 Organizer sign-in, event editor, form builder, responses table, export | not started |
| T7.4 Accessibility and low-end-phone pass (bundle size budget, keyboard, labels) | not started |

## Stage 8 — Tests, CI, docs

| Task | Status |
|---|---|
| T8.1 Unit tests: validation, capacity, duplicates, schema versioning, ticket validation, duplicate check-in, payment transitions, CSV | not started |
| T8.2 Rules tests run in CI | not started (B4) |
| T8.3 GitHub Actions: Android assemble + unit tests, web build + tests | not started — workflow can be written now; first green run depends on B2/B3 |
| T8.4 Docs: `PRODUCT_REQUIREMENTS`, `ARCHITECTURE`, `DATA_MODEL`, `SECURITY`, `TESTING_STRATEGY`, `DEPLOYMENT`, `ROADMAP`, updated README | not started |
| T8.5 Screenshots from synthetic data only | not started (needs a runnable build, B3) |

## Assumptions (conservative and reversible)

1. Keep XML + ViewBinding; no Compose/Flutter/React Native migration.
2. Use Firebase Authentication and Cloud Functions rather than a new standalone backend.
3. Existing Firestore documents are treated as disposable development data; any migration (T3.2, T4.3) will be written as a script and documented rather than run against live data.
4. Fonts and artwork will be properly licensed or generated in-repo; none copied from third parties.

## Decisions needed from the owner

1. Git identity to use (B1) and a valid push credential (B2).
2. Whether to proceed with Android changes that cannot be compiled here (flagged "written, not compiled"), or to start with the web client, whose tests can run here.
3. A Firebase project / emulator route (B4), and the payment provider (B5).
