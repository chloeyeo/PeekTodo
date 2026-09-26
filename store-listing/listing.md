# PeekTodo — Google Play default store listing

Everything below describes only what the app in this repository does today.
Verified against `AndroidManifest.xml`, `TodoNotifier.kt`, `SettingsRepository.kt`
and the Compose screens on the date of writing.

Not in the app, so not in the listing: a home-screen widget (a Glance
dependency is declared but no widget code exists), accounts, sync, reminders,
due dates, categories, and any network access (no INTERNET permission).

## App name

`PeekTodo: Lockscreen Todo App` — 29 characters (limit 30). Confirmed by count.

## Short description (limit 80)

1. `Your to-do list on the lock screen. Glance, unlock, get it done.` — 66
2. `See your open to-dos on the lock screen, with a blur mode for privacy.` — 72
3. `A simple to-do list that stays in view on your lock screen.` — 61

## Full description (limit 4000)

```
PeekTodo keeps your open to-dos in view on the lock screen, so you see what needs doing every time you pick up your phone.

It works through a single, quiet notification. Add tasks in the app and the notification shows them on the lock screen and in the notification shade: the number of open tasks, then the tasks themselves. Tick one off in the app and the notification updates right away.

Blur mode is for privacy. Turn it on and the lock screen shows only how many tasks are open, not what they are. Unlock your phone and the full list is back in the shade. Open the app from the notification and your tasks reveal themselves one by one.

Pin the notification if you want it to stay put. With pinning on, the notification comes straight back if it is swiped away or cleared. With pinning off, it behaves like any other notification.

What you can do

- Add a task in one line
- Edit a task in place
- Tick tasks off, and see finished ones in their own section
- Delete tasks you no longer need
- Keep the list on the lock screen through restarts and app updates

Simple and private

- No account and no sign-in
- No internet permission: your tasks never leave your phone
- Nothing to configure beyond two switches: blur mode and pin notification

Permissions and why

- Notifications: to show your to-do list as a notification on the lock screen and in the shade. Without it the app still works as a plain list.
- Run at startup: to put the notification back after your phone restarts.

The lock screen itself is unchanged. PeekTodo only posts a notification, which Android shows on your lock screen the same way it shows any other. If your phone hides notification content on the lock screen, blur mode still shows the count.
```

Character count: 1,735 (limit 4,000). Counted by script; re-run `python -c` in the repo if you edit it.

## Metadata policy self-review

Checked against Google Play's Metadata policy (play.google.com/about/developer-content-policy → Store Listing and Promotion → Metadata):

- No superlatives, rankings or performance claims ("best", "#1", "top", "fastest").
- No price or "free" wording.
- No emoji, repeated punctuation, ALL CAPS, or keyword lists.
- No other app, brand or device names in title or descriptions.
- No claim that the app replaces, locks, or secures the lock screen; it is described as a notification the system displays, and the last paragraph says so explicitly.
- Permissions listed are exactly the two the manifest requests, each with its purpose. No sensitive permissions are requested.
- Every feature mentioned exists in the code: notification with count and task lines, blur mode (plain count on the lock screen), pin notification with re-post on dismiss, restart/update re-post, in-app add/edit/complete/delete, no network access.

## Screenshot captions

Used by `make_screenshots.py` (matched on the raw file name):

| Raw file name contains | Caption |
|---|---|
| `blur` | Blur mode: only the count shows until you unlock |
| `lock` | Your to-dos, right on the lock screen |
| `list` | Add, tick off and tidy up inside the app |
| `edit` | Edit a task in place |
| `settings` | Two switches. That's the whole settings screen |
| `empty` | All clear. Shiba's keeping watch |

A "home screen widget" shot was requested. There is no widget in the app, so
there is nothing to capture and no caption for it.
