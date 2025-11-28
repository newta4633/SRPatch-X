package com.htetz.srpatchx.ui.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.FolderDelete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.htetz.srpatchx.R
import com.htetz.srpatchx.models.FileItem
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun FileDeleteConfirmDialog(
    item: FileItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val file = item.file
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                if (file.isDirectory) Icons.TwoTone.FolderDelete else Icons.TwoTone.Delete,
                null
            )
        },
        title = {
            Text(
                stringResource(R.string.delete_confirm_title)
            )
        },
        text = {
            MarkdownText(stringResource(R.string.delete_confirm_message, file.name))
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(stringResource(R.string.delete_confirm_btn))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        },
    )
}