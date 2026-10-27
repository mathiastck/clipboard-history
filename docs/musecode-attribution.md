# Muse Code attribution log

Running record of work done with Muse Code in this repo, for the hackathon
submission. One entry per task: date, what was asked, files touched, what changed.

## 2026-10-26

- **Scrubbed personal info from the handoff prompt.**
  Files: handoff prompt (moved out of the repo before the base commit, not part of it)
  Removed name/employer references; kept technical background (overlay surfaces,
  Kotlin/Java) and device context. Verified zero remaining matches.
- **Set up the Drive message bus (Muse app phone/VM ⇄ Mac Muse Code).**
  Files: Drive `FACEBOOK/MetaAI2026/musecode-mac-bus-protocol.md`,
  `to-mac/`, `from-mac/` (+ `STATUS.md`). No repo files.
  Convention for file-based task handoff between agents; plain markdown only.
- **Mirrored the pitch into the repo.**
  Files: `docs/pitch.md` (new), `docs/musecode-attribution.md` (new, this file)
  Distilled the public project description v1.0 PDF (on Drive) so the submission
  tree carries the pitch without the binary.
