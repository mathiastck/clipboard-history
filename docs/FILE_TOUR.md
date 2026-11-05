# ClipboardHistory — File Tour (pre-publish read-through)

For Matt's self-review before first public push.
Package `com.mattkanninen.cliphistory`, single module `:app`.
Generated 2026-11-04 by Muse Code. No code changed for this tour.

## How to read this

Start at [settings.gradle.kts](../settings.gradle.kts) → [app/build.gradle.kts](../app/build.gradle.kts) → [AndroidManifest.xml](../app/src/main/AndroidManifest.xml) → UI → data → DI → services → test.

## Root

- [settings.gradle.kts](../settings.gradle.kts) — root name `ClipboardHistory`, includes `:app` only.
- [build.gradle.kts](../build.gradle.kts) — root plugin declarations.
- [app/build.gradle.kts](../app/build.gradle.kts) — `compileSdk/targetSdk 35`, `minSdk 26`, Java/Kotlin 21, Compose + Hilt + Room/KSP. Release uses minify. Bottom has the Studio sync workaround task `prepareKotlinBuildScriptModel` (no-op, see commit `b7a380a`).
- [AGENTS.md](../AGENTS.md) — standing agent rules. Ships in the repo. Confirm you want this public.
- [LICENSE](../LICENSE) — Apache-2.0, added Oct 26.
- [.gitignore](../.gitignore) — covers `.gradle`, `local.properties`, `build/`, `.idea/`, `.kotlin/`. Good for publish.

## Manifest + permissions

- [AndroidManifest.xml](../app/src/main/AndroidManifest.xml) — declares `SYSTEM_ALERT_WINDOW`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`. Explicit comment: no `INTERNET` permission. Three components: `MainActivity`, `ClipboardAccessibilityService`, `ClipboardTileService`, `OverlayWindowService`.

## UI (Compose + Material 3)

- [MainActivity.kt](../app/src/main/java/com/mattkanninen/cliphistory/ui/main/MainActivity.kt) — `ComponentActivity`, Hilt entry point, collects `viewModel.uiState`, renders `ClipHistoryScreen`.
- [ClipHistoryViewModel.kt](../app/src/main/java/com/mattkanninen/cliphistory/ui/main/ClipHistoryViewModel.kt) — `searchQuery` + `selectedCategory` flows, `flatMapLatest` into `repository.observeAll/search`, `combine` with in-memory category filter (`All/Text/URL/Code`). Actions: search, category, togglePin, delete, clearUnpinned.
- [ClipHistoryScreen.kt](../app/src/main/java/com/mattkanninen/cliphistory/ui/components/ClipHistoryScreen.kt) — `Scaffold` + search field + `FilterChip` row + `LazyColumn` of `ClipItemCard`. Tap card copies back to clipboard.
- [Theme.kt](../app/src/main/java/com/mattkanninen/cliphistory/ui/theme/Theme.kt), [Color.kt](../app/src/main/java/com/mattkanninen/cliphistory/ui/theme/Color.kt), [Type.kt](../app/src/main/java/com/mattkanninen/cliphistory/ui/theme/Type.kt) — Material 3 theme.
- [strings.xml](../app/src/main/res/values/strings.xml), [colors.xml](../app/src/main/res/values/colors.xml), [styles.xml](../app/src/main/res/values/styles.xml) — app name is now `ClipboardHistory`.

## Data (Room) — READ THIS BEFORE PUBLISH

There are **two parallel stacks**. Only one is wired.

Active (used by repo + test + `DatabaseModule`):

- [ClipboardEntry.kt](../app/src/main/java/com/mattkanninen/cliphistory/data/ClipboardEntry.kt) — entity `clipboard_entries`, fields `content/timestamp/isPinned/contentHash/sourcePackage`.
- [ClipboardDao.kt](../app/src/main/java/com/mattkanninen/cliphistory/data/ClipboardDao.kt) — `observeAll/search/getLatest/findByHash/insert/updateTimestamp/togglePin/deleteById/clearUnpinned`.
- [AppDatabase.kt](../app/src/main/java/com/mattkanninen/cliphistory/data/AppDatabase.kt) — `clipboard_database.db`.
- [ClipRepository.kt](../app/src/main/java/com/mattkanninen/cliphistory/data/repository/ClipRepository.kt) + [ClipRepositoryImpl.kt](../app/src/main/java/com/mattkanninen/cliphistory/data/repository/ClipRepositoryImpl.kt) — trim → sha256 via [HashUtils.kt](../app/src/main/java/com/mattkanninen/cliphistory/data/HashUtils.kt) → `findByHash` dedupe (bump timestamp) else insert.
- [ClipRepositoryTest.kt](../app/src/test/java/com/mattkanninen/cliphistory/data/ClipRepositoryTest.kt) — MockK + Turbine + coroutines-test; covers dedupe insert, timestamp bump, togglePin.

Legacy / unwired (nothing injects or calls these today):

- [ClipEntity.kt](../app/src/main/java/com/mattkanninen/cliphistory/data/local/ClipEntity.kt) — entity `clipboard_history` with `category/sourceApp`.
- [ClipDao.kt](../app/src/main/java/com/mattkanninen/cliphistory/data/local/ClipDao.kt) — `getAllClips/searchClips/getPinnedClips/findByContent/insertClip/updatePinStatus/deleteClip/clearUnpinnedHistory/deleteExpiredClips`.
- [ClipDatabase.kt](../app/src/main/java/com/mattkanninen/cliphistory/data/local/ClipDatabase.kt) — `clipboard_history.db`.
- `AppModule.provideClipDatabase/provideClipDao` builds it, but `provideClipRepository` takes `ClipboardDao`, so the `ClipDatabase` instance is never consumed.

**REVIEW:** delete `data/local/` + the two `AppModule` providers, or finish the migration. Shipping both databases doubles review surface and will raise "which table is real?" on first read. Expiry (`deleteExpiredClips`) only exists on the unwired side.

## DI

- [DatabaseModule.kt](../app/src/main/java/com/mattkanninen/cliphistory/di/DatabaseModule.kt) — provides `AppDatabase` + `ClipboardDao`. This is the live path.
- [AppModule.kt](../app/src/main/java/com/mattkanninen/cliphistory/di/AppModule.kt) — provides `ClipDatabase`/`ClipDao` (unused, see above) and the `ClipRepository` binding. **REVIEW:** two database modules for one repo; merge into one after the data decision.
- [ClipHistoryApplication.kt](../app/src/main/java/com/mattkanninen/cliphistory/ClipHistoryApplication.kt) — `@HiltAndroidApp`.

## Services (capture + surfaces)

- [ClipboardAccessibilityService.kt](../app/src/main/java/com/mattkanninen/cliphistory/service/ClipboardAccessibilityService.kt) — listens `OnPrimaryClipChanged` + accessibility events, saves non-blank text via repo on IO scope. Declared in manifest with `BIND_ACCESSIBILITY_SERVICE`, config in [accessibility_service_config.xml](../app/src/main/res/xml/accessibility_service_config.xml).
- [ClipboardTileService.kt](../app/src/main/java/com/mattkanninen/cliphistory/service/ClipboardTileService.kt) — QS tile tap starts `OverlayWindowService` as foreground service.
- [OverlayWindowService.kt](../app/src/main/java/com/mattkanninen/cliphistory/service/OverlayWindowService.kt) — foreground service + `TYPE_APPLICATION_OVERLAY` `ComposeView`. **REVIEW:** `setContent` is an empty stub today; the panel UI from the pitch is not implemented yet. Publish as MVP-with-stub or hold the service out of the manifest until it renders.

## Resources

- `res/drawable/ic_clipboard_tile.xml`, launcher icons under `res/mipmap-*`, `res/xml/accessibility_service_config.xml`.

## Muse Code contributions (hackathon record)

Full running log lives in [musecode-attribution.md](./musecode-attribution.md). What Muse Code did here:

- 2026-10-26: scrubbed personal info from handoff prompt; set up Drive bus protocol (`to-mac/`/`from-mac/`); mirrored pitch into [pitch.md](./pitch.md) + created the attribution log.
- 2026-10-26–30: Apache-2.0 `LICENSE`; Studio sync workaround (`prepareKotlinBuildScriptModel`); rename to `ClipboardHistory` (commit `8690667`); bus status replies.
- 2026-11-04: this tour (`docs/FILE_TOUR.md`). No source edits.

Non-Muse-Code work (yours): the `:app` feature code, manifest, resources, and the `data/local` prototype predate this tour.

## Suggested pre-publish checklist

1. Decide data stack: keep `Clipboard*`, remove `data/local/` + unused providers.
2. Decide overlay: stub `setContent` vs. first panel UI vs. remove service from manifest for v0.1.
3. Confirm `AGENTS.md` stays public.
4. Run `./gradlew assembleDebug` when you ask (skipped per your rule).
5. Create empty public `mathiastck/ClipboardHistory` (no README/gitignore/license), hand URL, then `git remote add origin` + verified push.
