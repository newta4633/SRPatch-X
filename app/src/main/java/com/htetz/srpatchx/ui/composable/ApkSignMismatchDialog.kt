package com.htetz.srpatchx.ui.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.htetz.srpatchx.R

@Composable
fun ApkSignMismatchDialog(
    onDismiss: () -> Unit,
    onUninstall: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.TwoTone.Warning, contentDescription = null) },
        title = { Text(stringResource(R.string.apk_sign_mismatch_title)) },
        text = { Text(stringResource(R.string.apk_sign_mismatch_msg)) },
        confirmButton = {
            FilledTonalButton(onClick = onUninstall) {
                Text(stringResource(R.string.btn_uninstall))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_close))
            }
        }
    )
}