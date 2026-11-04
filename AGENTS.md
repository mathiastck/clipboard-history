# Agent Guidelines & Repository Context (`AGENTS.md`)

This file contains operational guidelines, architectural standards, and developer preferences for AI agents working in this repository.

---

## 1. Developer Context & Preferences
- **Developer:** Matt Kanninen (Lead / Senior Android Software Engineer)
- **Career Highlights:**
  - **Walgreens (2023 - 2026):** Lead Android Engineer. Jetpack Compose homescreen redesign, coupons, native Google Ads, 2FA/login, secure AI tooling adoption.
  - **Google (2019 - 2023):** Senior SWE L5 on Google TV / LauncherX. Rich notifications, cloud emulator Espresso test suites across Google TV, Play, and Auto.
  - **Meta / Facebook (2017 - 2019):** SWE E6. Messenger Stories/Day, Chat Heads lead, Android Bubbles OS feature implementation, Portal calling.
  - **Android Veteran:** Building Android applications since pre-1.0 Beta / Cupcake (2008), Myspace, Grindr v3 rewrite, DIRECTV, Scopely, Logitech (Google TV Revue).
- **Preferences & Agent Rules:**
  - *Belt-and-Suspenders Safeguard:* Matt wants Gemini to suggest a belt-and-suspenders approach for even slightly risky actions — redundant safeguards or verification beyond the minimum necessary — before proceeding.
  - *User Feedback / Confirmation:* Thumbs up actions should be user-definable, acting as an "OK / Proceed" confirmation to execute the proposed agentic step.
  - *Target:* Building a Pixel-optimized, Maccy-style Clipboard History App with zero-network privacy and modern Android practices.

---

## 2. Project Overview
- **Project Name:** ClipboardHistory
- **Package Name:** `com.mattkanninen.cliphistory`
- **Module Structure:** Single module (`:app`) with clean separation of layers (`data/`, `service/`, `ui/`, `di/`).
- **Core Functionality:**
  - **Zero Network Permissions:** 100% local and private clipboard data.
  - **Background Capture:** Accessibility Service for real-time copy event capture across all apps.
  - **Quick Access:** Quick Settings Tile + Floating Material 3 Compose Edge Overlay (`SYSTEM_ALERT_WINDOW`).
  - **Storage:** Room Database with full-text search, pinning, category tagging, and automatic expiry for sensitive clips.

---

## 3. Code & Architecture Guidelines
- **Language:** Kotlin 2.1+
- **UI Framework:** Jetpack Compose + Material 3 (with Dynamic Color / Monet integration)
- **Architecture:** MVVM / Clean Architecture
- **Async & State:** Kotlin Coroutines, Flow / StateFlow
- **Dependencies & Injection:** Hilt
- **Storage:** Room Database with KSP

---

## 4. Agentic Execution Loop
1. **Scope:** Inspect dependencies, types, and module boundaries before code edits.
2. **Modify:** Apply targeted, surgical diffs.
3. **Verify:** Ensure builds compile cleanly (`./gradlew assembleDebug`).
4. **Audit:** Inspect layout trees and screen captures for visual correctness.
