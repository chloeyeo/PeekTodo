package com.chloeyeo.peektodo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.chloeyeo.peektodo.ui.theme.shiba

/**
 * A card styled like the icon's clipboard: off-white, generously rounded,
 * with a thin ink outline.
 */
@Composable
fun ClipboardCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.shiba.card,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.5.dp, MaterialTheme.shiba.cardOutline),
        content = content,
    )
}

/**
 * Rounded checkbox matching the icon's: cream square with an ink outline
 * and a blue tick. Drop-in for Material's Checkbox (same toggle semantics).
 */
@Composable
fun ShibaCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(7.dp)
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .size(24.dp)
            .background(MaterialTheme.shiba.checkboxFill, shape)
            .border(2.dp, MaterialTheme.shiba.checkboxOutline, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.shiba.tick,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
