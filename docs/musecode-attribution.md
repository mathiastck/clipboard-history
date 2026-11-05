# Muse Code attribution log

Matt wrote no code in this repo. Every line of code, config, and docs
was written by Muse Code, driven by Matt's prompts. Starting point was
an empty project scaffold that Gemini in Android Studio created before
the hackathon. Muse Code documented the process from there.

Running record, one entry per task: date, what was asked, files touched,
what changed.

## 2026-10-11 — Initial app (Muse Code, from empty scaffold)

- **Built the full ClipboardHistory app from the empty Gemini scaffold.**
  Files: `app/src/main/AndroidManifest.xml`, `app/build.gradle.kts`,
  all Kotlin under `app/src/main/java/com/mattkanninen/cliphistory/`,
  all res under `app/src/main/res/`, `settings.gradle.kts`,
  `build.gradle.kts`, `gradle/libs.versions.toml`
- Data layer: `data/ClipboardEntry.kt` (`clipboard_entries`,
  `content/timestamp/isPinned/contentHash/sourcePackage`),
  `data/ClipboardDao.kt` (`observeAll/search/getLatest/findByHash/
  insert/updateTimestamp/togglePin/deleteById/clearUnpinned`),
  `data/AppDatabase.kt` (`clipboard_database.db`),
  `data/HashUtils.kt` (SHA-256 trim-then-hash dedupe),
  `data/repository/ClipRepository.kt` +
  `data/repository/ClipRepositoryImpl.kt` (dedupe: `findByHash`,
  bump timestamp on re-copy, else insert).
- Legacy second stack (kept, unwired): `data/local/ClipEntity.kt`
  (`clipboard_history`), `data/local/ClipDao.kt`
  (`getAllClips/searchClips/getPinnedClips/findByContent/
  insertClip/updatePinStatus/deleteClip/clearUnpinnedHistory/
  deleteExpiredClips`), `data/local/ClipDatabase.kt`
  (`clipboard_history.db`). Nothing injects or calls it today.
- DI: `di/DatabaseModule.kt` (live `AppDatabase` + `ClipboardDao`
  path), `di/AppModule.kt` (`ClipDatabase`/`ClipDao` providers plus
  `ClipRepository` binding), `ClipHistoryApplication.kt`
  (`@HiltAndroidApp`).
- Capture + surfaces: `service/ClipboardAccessibilityService.kt`
  (copy-event capture across apps, window-focus checks),
  `service/ClipboardTileService.kt` (QS tile),
  `service/OverlayWindowService.kt` (floating panel,
  `SYSTEM_ALERT_WINDOW`, `PixelFormat.TRANSLUCENT`),
  `res/xml/accessibility_service_config.xml`, manifest entries for
  all three components plus `MainActivity`, permissions
  (`SYSTEM_ALERT_WINDOW`, `POST_NOTIFICATIONS`,
  `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`,
  explicitly no `INTERNET`).
- UI: `ui/main/MainActivity.kt` (Hilt entry, collects
  `viewModel.uiState`), `ui/main/ClipHistoryViewModel.kt`
  (`searchQuery` + `selectedCategory` flows, `flatMapLatest` into
  `observeAll/search`, `combine` with in-memory category filter
  `All/Text/URL/Code`, actions search/category/togglePin/delete/
  clearUnpinned), `ui/components/ClipHistoryScreen.kt` (`Scaffold` +
  search field + `FilterChip` row + `LazyColumn` of `ClipItemCard`,
  tap card copies back, per-card category label), `ui/theme/`
  (`Theme.kt`, `Color.kt`, `Type.kt`), `res/values/`
  (`strings.xml`, `colors.xml`, `styles.xml`).
- Test: `app/src/test/.../ClipRepositoryTest.kt` (MockK + Turbine +
  coroutines-test; dedupe insert, timestamp bump, togglePin).
- Matt's role: prompts, on-device checks (service on/off,
  screenshots), submission direction. No hand-written code.

## 2026-10-26

- **Recorded the pre-Muse-Code base state.**
  Files: history checkpoint `e3363bf Base: pre-Muse-Code
  pre-hackathon state`. No source edits by Matt.
- **Scrubbed personal info from the handoff prompt.**
  Files: handoff prompt (moved out of the repo before the base
  commit, not part of it)
  Removed name/employer references; kept technical background
  (overlay surfaces, Kotlin/Java) and device context (Pixel 10 Pro).
  Verified zero remaining matches.
- **Set up the Drive message bus (Muse app phone/VM ⇄ Mac Muse Code).**
  Files: Drive `FACEBOOK/MetaAI2026/musecode-mac-bus-protocol.md`,
  `to-mac/`, `from-mac/` (+ `STATUS.md`). No repo files.
  Convention for file-based task handoff between agents; plain
  markdown only. Later extended to Muse mobile on Pixel 10 Pro via
  shared Google Drive.
- **Mirrored the pitch into the repo.**
  Files: `docs/pitch.md` (new), `docs/musecode-attribution.md`
  (new, this file)
  Distilled the public project description v1.0 PDF (on Drive) so
  the submission tree carries the pitch without the binary:
  persistent clipboard history that beats Gboard one-hour expiry,
  panel/bubble/tile/in-app channels, Play contingency video plan.

## 2026-10-29

- **Added Apache-2.0 LICENSE.**
  Files: `LICENSE` (new, `9ec8aff`). Copyright Matt Kanninen.
- **Fixed Android Studio sync.**
  Files: `app/build.gradle.kts` (`b3138f4`)
  Registered empty `prepareKotlinBuildScriptModel` on `:app`, no-op
  workaround so Studio sync passes. No behavior change.
- **Iterated to green builds.**
  Muse Code ran builds, read failures, fixed config/code until
  compile passed. Matt approved runs, did not edit code. Build
  outputs stayed untracked via `.gitignore`
  (`build/`, `.gradle`, `local.properties`, `.idea/`, `.kotlin/`).

## 2026-11-04

- **Renamed app display name to ClipboardHistory.**
  Files: `c062ac1` (was `8690667` pre-scrub): manifest, theme,
  strings, database name, `rootProject.name`, 10 files total.
  User-visible `Pixel Clip History` → `Clipboard History`.
- **Wrote the pre-publish file tour.**
  Files: `docs/FILE_TOUR.md` (new, untracked at first)
  Full walk of `:app` (manifest, UI, dual Room stacks, DI,
  services, test) with REVIEW flags: duplicate `Clipboard*` vs
  `data/local` stacks, empty overlay `setContent` stub, duplicated
  category heuristic (`http`, `{`, `val `, `fun ` in both ViewModel
  filter and Screen label; `Text` chip currently unfiltered vs
  label). No source edits.
- **Started submission record.**
  Files: `docs/submission.md` (new)
  Submission line, track (Build for Your World), YouTube channel,
  describe-your-use, Meta-models importance, feedback — all dictated
  by Matt, written by Muse Code.

## 2026-11-05 — Pre-publish scrub (Muse Code, Matt approved)

- **Removed machine crud from GitHub-visible history, kept history.**
  Before: root `68e12c0` tracked 2,101 `app/build/` blobs with
  absolute paths (`/Users/kanninen/...PixelClipHistory/...`,
  `/Users/kanninen/.gradle/...`). After: root `3d64198`
  `Initial commit: Clipboard History ...` (49 files, no builds),
  empty `stop tracking build outputs` dropped. 6 commits → 5.
  Author kept as Matt Kanninen `<mathiastck@hotmail.com>`.
  Verified `HEAD`-only: no `build/` objects, no `/Users/kanninen`
  strings, no `Pixel` in messages. Remaining `Pixel`: target-device
  `Pixel-optimized` in `AGENTS.md` and Android API `PixelFormat`
  (both intentional), plus honest rename diff.
- **Backups kept locally, never pushed.**
  `../PixelClipHistory.bundle` (88M, verified) +
  `../PixelClipHistory/` browsable clone hold the full 6-commit
  original. Recovery ref `refs/tbh/recovery/before-discard/...`
  retained. Canonical untouched except the authorized rewrite;
  dirty `docs/musecode-attribution.md` + untracked
  `docs/FILE_TOUR.md` / `docs/submission.md` preserved via
  autostash.
- **No secrets introduced.** Tracked tree has no API keys,
  passwords, `google-services.json`, keystores. `local.properties`
  (`sdk.dir=/Users/kanninen/...`) stays untracked. No `INTERNET`
  permission. No commits/pushes without Matt's explicit word;
  pushes require URL + two confirms.
