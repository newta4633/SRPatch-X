package com.htetz.srpatchx.ui.composable

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Cancel
import androidx.compose.material.icons.twotone.Check
import androidx.compose.material.icons.twotone.FolderCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.htetz.srpatchx.R
import com.htetz.srpatchx.domain.VersionConst

@Composable
fun StoragePermissionDialog(
    onChange: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val writeStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            onChange()
        }
    )

    val accessStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            onChange()
        }
    )

    AlertDialog(
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        modifier = modifier,
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.TwoTone.FolderCopy, null) },
        title = { Text(stringResource(R.string.permission_storage_title)) },
        text = {
            Text(stringResource(R.string.permission_storage_msg))
        },
        confirmButton = {
            Button(
                onClick = {
                    if (VersionConst.atLeastAndroid11()) {
                        accessStorageLauncher.launch(
                            Intent(
                                android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                "package:${context.packageName}".toUri()
                            )
                        )
                    } else {
                        writeStorageLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            ) {
                Icon(Icons.TwoTone.Check, null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.btn_grant))
            }
        },
        dismissButton = {
            OutlinedButton(
                enabled = false,
                onClick = onDismiss
            ) {
                Icon(Icons.TwoTone.Cancel, null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}