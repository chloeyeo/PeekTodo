package com.chloeyeo.peektodo.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chloeyeo.peektodo.R
import com.chloeyeo.peektodo.ui.components.ClipboardCard
import com.chloeyeo.peektodo.ui.theme.shiba

/** Public policy page, hosted from docs/privacy.html in this repo via GitHub Pages. */
const val PRIVACY_POLICY_URL = "https://chloeyeo.github.io/PeekTodo/privacy.html"

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val context = LocalContext.current
    val blurMode by viewModel.blurMode.collectAsStateWithLifecycle()
    val pinNotification by viewModel.pinNotification.collectAsStateWithLifecycle()
    SettingsScreen(
        blurMode = blurMode,
        pinNotification = pinNotification,
        onBlurModeChange = viewModel::setBlurMode,
        onPinNotificationChange = viewModel::setPinNotification,
        onOpenPrivacyPolicy = {
            val intent = Intent(Intent.ACTION_VIEW, PRIVACY_POLICY_URL.toUri())
            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                // No browser installed; nothing sensible to do.
            }
        },
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    blurMode: Boolean,
    pinNotification: Boolean,
    onBlurModeChange: (Boolean) -> Unit,
    onPinNotificationChange: (Boolean) -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            SettingCard(
                title = stringResource(R.string.settings_blur_title),
                subtitle = stringResource(R.string.settings_blur_subtitle),
                checked = blurMode,
                onCheckedChange = onBlurModeChange,
            )
            SettingCard(
                title = stringResource(R.string.settings_pin_title),
                subtitle = stringResource(R.string.settings_pin_subtitle),
                checked = pinNotification,
                onCheckedChange = onPinNotificationChange,
            )
            LinkCard(
                title = stringResource(R.string.settings_privacy_title),
                subtitle = stringResource(R.string.settings_privacy_subtitle),
                onClick = onOpenPrivacyPolicy,
            )
        }
    }
}

@Composable
private fun SettingCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ClipboardCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.shiba.card,
                headlineColor = MaterialTheme.colorScheme.onSurface,
                supportingColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(subtitle) },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.shiba.card,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedBorderColor = MaterialTheme.shiba.cardOutline,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.shiba.checkboxFill,
                        uncheckedBorderColor = MaterialTheme.shiba.cardOutline,
                    ),
                )
            },
        )
    }
}

/** A card that opens an external page; Play requires the privacy policy to be reachable in-app. */
@Composable
private fun LinkCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    ClipboardCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        ListItem(
            modifier = Modifier.clickable(onClick = onClick),
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.shiba.card,
                headlineColor = MaterialTheme.colorScheme.onSurface,
                supportingColor = MaterialTheme.colorScheme.onSurfaceVariant,
                trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(subtitle) },
            trailingContent = {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            },
        )
    }
}
