# Pitch (distilled)

> "Secure local dump everything notes app with clipboard history, biometrics."

Distilled 2026-10-26 from the public project description v1.0 (PDF, lives on Drive).
Meta Global AI Developer Hackathon — Indie Dev Track ("Push the Frontier"). Approved entry.

## The idea

Most apps pick a surface and trap their content inside it. This project inverts that:
the clipboard list is the product. The panel, the bubble, the tile, and the in-app
view are just channels for showing it — and the user picks the channel, changes
their mind, or picks fresh each time. Scope growth here is the good kind: the core
stays a simple little list; what grows is the number of ways the list can be seen.

## Surfaces: three layers, one list

| Layer | Surface | Role |
|---|---|---|
| Primary | Overlay panel (SAW) | Always-available handle + history browser. User-initiated, collapsible, not full-screen, not on lock screen. |
| Secondary | Bubbles | Blessed floating path tied to a notification. Good for conversation-shaped surfaces (pinned/current clip). No overlay permission needed. |
| Fallback | QS Tile + notification | No floating UI, no overlay permission. Tile tap opens history. Keeps working when the other two cannot. |

Each layer keeps the promise in a more degraded form than the last. The app never
guesses which UI is best — the user says so.

## Liveness contract

"Supposed to be open right now." When a user opts in to a floating surface, that
surface is open now — not usually, not when the system feels generous. Every
restart and liveness technique exists to honor that sentence. A handle that vanished
because the system killed it is worse than no handle: it breaks an accepted promise.
Doze, app standby, background restrictions, and the Android 15 overlay rule keep
renegotiating the contract — which is why fallback layers are part of the design,
not an afterthought.

Out of scope: building a standalone keyboard. Contributing the list to an existing
open-source keyboard later is a different, explicitly deferred move.

## Permissions, plainly

- Overlay is `android.permission.SYSTEM_ALERT_WINDOW`, granted via
  `Settings.ACTION_MANAGE_OVERLAY_PERMISSION`. The user flips the toggle manually;
  it cannot be auto-granted.
- Since ~2020 Play also requires a genuinely overlay-needing core feature with
  disclosure. A clipboard side panel qualifies.
- Android 15 wrinkle: with SAW, a foreground service can only start when a visible
  overlay exists. This shapes how the clipboard watcher is wired to its service.
- Bubbles (Android 11+, `BubbleMetadata`) are the blessed floating path but only
  for conversations/notifications — hence the hybrid.

## Play contingency + the video

Play review is not needed for the hackathon (demo is sideloaded), but the overlay
permission would trigger Play Console's Permissions declaration form if ever
published — including a demo video (step 4), reviewer instructions, and an extended
review of up to several weeks. Strategy: record the footage while building (free
then), treat Play as a contingency — filed, not chased. One video, three audiences:
the hackathon demo (list via panel, bubble, tile in one recording) doubles as the
future declaration video.

## Build focus

1. Local clipboard history, on-device, with biometric unlock for the notes view.
2. Overlay panel first, Bubbles second, QS Tile + notification fallback.
3. Demo: one recording, same list through panel, bubble, and tile.

## Timeline

- Submission due: November 4, 2026 (9pm ET).
- Judging: November 4–8. Closing ceremony: November 9.
- Announcements: Threads (build updates/discussion) and LinkedIn (announcement).
