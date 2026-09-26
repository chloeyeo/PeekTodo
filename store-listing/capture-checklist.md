# Screenshot capture checklist

Play wants phone screenshots at exactly 9:16 with the short side at least 1080 px,
so capture on a **Pixel 2 emulator (1080x1920)**, not the Galaxy A06 (720x1600,
9:20). The pipeline never stretches, so an off-ratio source would be letterboxed.

Emulators (created by `avdmanager`, also visible in Android Studio's Device Manager):

- `Pixel_2_API_34` — phone, 1080x1920
- `Pixel_Tablet_API_34` — tablet, 2560x1600 (letterboxed to 1920x1080 by the script)

## One-time setup per emulator

```
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell pm grant com.chloeyeo.peektodo android.permission.POST_NOTIFICATIONS
adb shell locksettings set-pin 1234
```

Open the app and add realistic tasks, for example:
`Pay electricity bill`, `Email Minji the deck`, `Gym 7pm`, `Book dentist`.

Clean status bar (demo mode: fixed clock, full battery, no stray icons):

```
adb shell settings put global sysui_demo_allowed 1
adb shell am broadcast -a com.android.systemui.demo -e command enter
adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 1000
adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false
adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 -e mobile show -e datatype none -e level 4
adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false
```

Capture (Windows PowerShell mangles `exec-out` binary output, so save on the device and pull):

```
adb shell screencap -p /sdcard/shot.png
adb pull /sdcard/shot.png store-listing/raw/01-lockscreen.png
```

Leave demo mode when finished: `adb shell am broadcast -a com.android.systemui.demo -e command exit`
and clear the PIN: `adb shell locksettings clear --old 1234`.

## Phone shots (drop into `store-listing/raw/`)

| # | File name | State to set up | How to get there |
|---|---|---|---|
| 1 | `01-lockscreen.png` | Lock screen, blur off, 3–4 open tasks, notification expanded | Settings: blur off. `adb shell input keyevent 26` then `224`. Tap the notification's chevron to expand. |
| 2 | `02-lockscreen-blur.png` | Lock screen, blur on: card reads "PeekTodo / 4 tasks" | Settings: blur on. Lock and wake as above. Do not expand. |
| 3 | `03-list.png` | In-app list with open tasks and one in the Done section | Tick one task off, keyboard closed. |
| 4 | `04-settings.png` | Settings screen showing both switches | Tap the gear. |
| 5 | `05-edit.png` (optional) | A row in edit mode with the keyboard up | Tap a pencil. |
| 6 | `06-empty.png` (optional) | Empty state with the shiba illustration | Delete all tasks. |

Four is the minimum for promotion eligibility; shots 1–4 cover it.
There is no home-screen widget in the app, so there is no widget shot.

Unlocking the emulator from the command line: `adb shell input keyevent 82`,
`adb shell input text 1234`, `adb shell input keyevent 66`.

## Tablet shots (drop into `store-listing/raw/tablet/`)

Same app, same states, captured on `Pixel_Tablet_API_34` in landscape:
`01-list.png`, `02-settings.png`, and optionally `03-lockscreen.png`.
The script outputs 1920x1080 for landscape captures and 1080x1920 for portrait ones.

## Then

```
python store-listing/make_screenshots.py
```

It prints every output with its size and fails if anything is off-spec or over 8 MB.
