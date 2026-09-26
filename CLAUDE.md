# PeekTodo

Android app (Kotlin, Jetpack Compose, Room, DataStore) that surfaces open to-dos on the lock screen via a persistent notification. Package `com.chloeyeo.peektodo`, minSdk 26, compile/target 36.

## Git workflow (important)

- Claude MAY run `git add .`, `git add <files>`, `git status`, `git diff`, `git log`.
- Claude MUST NOT run `git commit`, `git push`, `git pull`, `git rebase`, `git reset`, `git checkout`, `git stash`, or anything else that changes history or the remote. The user does those personally.
- After staging, Claude ends its message with a suggested commit message: one concise line, imperative mood, under ~72 characters, no trailing period, no Co-Authored-By trailer. Format it in a code block so it can be copied straight into `git commit -m`.

## Build and verify

- Build: `.\gradlew.bat assembleDebug` (Gradle 8.14, AGP 8.13, Kotlin 2.2.20; works on the JDK 22 on PATH).
- Lint: `.\gradlew.bat lintDebug`, then read `app/build/reports/lint-results-debug.xml`.
- Emulator: AVD `Pixel_7` (API 34). Lock-screen behaviour must be checked on a real keyguard (set a temporary PIN with `locksettings set-pin`, clear with `locksettings clear --old`).

## Visual identity

- Source art: `design/shiba_wink.png`. Every colour lives in `ui/theme/Color.kt` (sampled hex values + derived AA-safe shades); do not add colours elsewhere. `MaterialTheme.shiba` exposes the card/checkbox/tick colours the Material scheme has no slot for.
- Icon: `drawable/ic_launcher_foreground.xml` (vector traced in the 1024 source coordinate space), `ic_launcher_monochrome.xml` (themed icons), `ic_notification.xml` (white silhouette). Legacy PNGs and the 512 store icon are regenerated with `powershell -File design\make_icons.ps1`.
- Dynamic colour is off on purpose so the app always matches its icon.

## Design guardrails

- Blur mode changes the notification text on the lock screen only. It must never alter the shade or post-unlock content, and nothing may pop up over the lock screen on its own (no full-screen intents, no showWhenLocked activities).
- The notification channel stays IMPORTANCE_DEFAULT with sound and vibration off. IMPORTANCE_LOW gets hidden from the keyguard as "silent".
- To-dos are added or edited only inside the app or the (planned) Glance widget, never from the lock screen. Editing is inline in the list row (pencil icon), one row at a time, blank text rejected.
- Pin notification (Settings toggle, default off): ongoing + deleteIntent that re-posts via `NotificationDismissedReceiver` while the setting is on. Off means a normal swipeable notification. No foreground service.
