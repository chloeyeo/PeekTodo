package com.chloeyeo.peektodo

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.chloeyeo.peektodo.notification.TodoNotifier
import com.chloeyeo.peektodo.ui.PeekTodoNavHost
import com.chloeyeo.peektodo.ui.theme.PeekTodoTheme

class MainActivity : ComponentActivity() {

    /**
     * Incremented each time the app is opened with the reveal extra (a
     * notification tap in blur mode). The list screen plays the staggered
     * reveal whenever this changes. Zero means "no animation".
     */
    private var revealKey by mutableIntStateOf(0)

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                (application as PeekTodoApp).container.notificationSync.refresh()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) consumeRevealExtra(intent)
        setContent {
            PeekTodoTheme {
                PeekTodoNavHost(revealKey = revealKey)
            }
        }
        if (savedInstanceState == null) requestNotificationPermissionIfNeeded()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeRevealExtra(intent)
    }

    private fun consumeRevealExtra(intent: Intent?) {
        if (intent?.getBooleanExtra(TodoNotifier.EXTRA_REVEAL_ON_LAUNCH, false) == true) {
            intent.removeExtra(TodoNotifier.EXTRA_REVEAL_ON_LAUNCH)
            revealKey++
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (TodoNotifier.canPost(this)) return
        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
