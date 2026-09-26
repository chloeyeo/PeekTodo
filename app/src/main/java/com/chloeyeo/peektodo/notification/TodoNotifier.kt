package com.chloeyeo.peektodo.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.chloeyeo.peektodo.MainActivity
import com.chloeyeo.peektodo.R
import com.chloeyeo.peektodo.data.Todo

/**
 * Builds and posts the single persistent "open to-dos" notification.
 *
 * Default mode: title "N tasks", expandable list of the open titles.
 * Blur mode: identical everywhere except on the lock screen, where it is a
 * plain single-line "PeekTodo / N tasks" with no expandable content. Two
 * mechanisms combine to make that lock-screen-only:
 *
 * 1. [NotificationCompat.Builder.setPublicVersion] with VISIBILITY_PRIVATE.
 *    Android swaps it in on the lock screen, but only when the user has set
 *    "hide sensitive content"; the OS default shows everything.
 * 2. While the keyguard is locked ([locked]) the notification's own content
 *    is the plain version, and it is re-posted with the full content on
 *    unlock. This covers the default setting whenever the process is alive.
 *
 * Pin mode: the notification is ongoing and carries a deleteIntent. Android 14+
 * lets users swipe ongoing notifications away, so [NotificationDismissedReceiver]
 * re-posts it as long as the setting is still on.
 */
object TodoNotifier {

    const val CHANNEL_ID = "open_todos"
    const val NOTIFICATION_ID = 1

    /** Boolean extra on the launch intent: play the staggered reveal on the list screen. */
    const val EXTRA_REVEAL_ON_LAUNCH = "com.chloeyeo.peektodo.extra.REVEAL_ON_LAUNCH"

    /** InboxStyle caps visible lines; beyond this we show a "+N more" summary. */
    private const val MAX_LINES = 5
    private const val REQUEST_OPEN_APP = 0
    private const val REQUEST_DISMISSED = 1

    /**
     * IMPORTANCE_DEFAULT, not LOW: SystemUI files LOW-importance notifications
     * under the "Silent" section, and the keyguard hides that whole section
     * when "hide silent notifications on lock screen" is on (the default on
     * the Android 14 emulator image). DEFAULT with sound and vibration
     * disabled is still quiet and never heads-up, but always reaches the
     * lock screen.
     */
    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
        }
        manager.createNotificationChannel(channel)
    }

    fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * Posts the notification for [openTodos], or cancels it when there is
     * nothing open. Safe to call from any thread.
     *
     * @param blurMode the user's blur setting.
     * @param locked whether the keyguard is currently locked (screen off counts).
     * @param pinned the user's pin setting: ongoing + re-post on dismiss.
     */
    fun show(
        context: Context,
        openTodos: List<Todo>,
        blurMode: Boolean,
        locked: Boolean,
        pinned: Boolean,
    ) {
        val manager = NotificationManagerCompat.from(context)
        if (openTodos.isEmpty()) {
            manager.cancel(NOTIFICATION_ID)
            return
        }
        if (!canPost(context)) return

        val contentIntent = openAppIntent(context, revealOnLaunch = blurMode)
        val builder = if (blurMode && locked) {
            plainBuilder(context, openTodos.size, pinned)
        } else {
            fullBuilder(context, openTodos, pinned)
        }
        builder.setContentIntent(contentIntent)

        if (blurMode) {
            val publicVersion = plainBuilder(context, openTodos.size, pinned)
                .setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()
            builder
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPublicVersion(publicVersion)
        } else {
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }

        // Permission is checked in canPost(); notify() is annotated for lint only.
        @Suppress("MissingPermission")
        manager.notify(NOTIFICATION_ID, builder.build())
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    /**
     * Everything the two variants share: channel, icon, colour, quiet behaviour.
     * Pin mode adds the ongoing flag and the dismiss receiver; off means a
     * normal, swipeable notification whose dismissal is not intercepted.
     */
    private fun baseBuilder(context: Context, pinned: Boolean): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setOngoing(pinned)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
        if (pinned) builder.setDeleteIntent(dismissedIntent(context))
        return builder
    }

    /** Default mode: count as the title, open titles as expandable lines. */
    private fun fullBuilder(
        context: Context,
        openTodos: List<Todo>,
        pinned: Boolean,
    ): NotificationCompat.Builder {
        val titles = openTodos.map { it.title }
        return baseBuilder(context, pinned)
            .setContentTitle(taskCount(context, openTodos.size))
            .setContentText(titles.first())
            .setStyle(inboxStyle(context, titles))
    }

    /** Blur mode on the lock screen: app name + count, nothing expandable. */
    private fun plainBuilder(context: Context, count: Int, pinned: Boolean): NotificationCompat.Builder =
        baseBuilder(context, pinned)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(taskCount(context, count))

    private fun taskCount(context: Context, count: Int): String =
        context.resources.getQuantityString(R.plurals.task_count, count, count)

    private fun inboxStyle(context: Context, lines: List<String>): NotificationCompat.InboxStyle {
        val style = NotificationCompat.InboxStyle()
        lines.take(MAX_LINES).forEach(style::addLine)
        if (lines.size > MAX_LINES) {
            val extra = lines.size - MAX_LINES
            style.setSummaryText(context.resources.getQuantityString(R.plurals.notification_more, extra, extra))
        }
        return style
    }

    private fun openAppIntent(context: Context, revealOnLaunch: Boolean): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_REVEAL_ON_LAUNCH, revealOnLaunch)
        }
        return PendingIntent.getActivity(
            context,
            REQUEST_OPEN_APP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /** Fired by the system on swipe-away or "Clear all"; delivered even if the process is dead. */
    private fun dismissedIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_DISMISSED,
            Intent(context, NotificationDismissedReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
