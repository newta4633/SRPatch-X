package com.htetz.srpatchx.ui.composable

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.htetz.srpatchx.R

@Composable
fun ErrorDialog(
    throwable: Throwable,
    title: String,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        icon = {
            Icon(Icons.TwoTone.Error, contentDescription = null)
        },
        title = { Text(title) },
        text = {
            Text(
                text = throwable.message ?: throwable.stackTraceToString(),
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.btn_close))
            }
        }
    )
}