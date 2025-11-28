package com.htetz.srpatchx.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Build
import androidx.compose.material.icons.twotone.InstallMobile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.htetz.srpatchx.R
import com.htetz.srpatchx.models.FileItem

@Composable
fun ApkOpenDialog(
    apkItem: FileItem.Apk,
    onDismiss: () -> Unit,
    onPatch: () -> Unit,
    onInstall: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        icon = {
            AsyncImage(
                model = apkItem,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(apkItem.appName)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text("${apkItem.versionName} (${apkItem.versionCode})")
                    }
                    if (apkItem.isPatched) {
                        Badge {
                            Text(stringResource(R.string.label_patched))
                        }
                    }
                }
            }
        },
        text = {
            Text(
                stringResource(
                    R.string.apk_info_desc,
                    apkItem.minSdkVersion,
                    apkItem.targetSdkVersion,
                    apkItem.compileSdkVersion,
                    apkItem.packageName
                )
            )
        },
        dismissButton = {
            OutlinedButton(
                onClick = onInstall
            ) {
                Icon(Icons.TwoTone.InstallMobile, null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.btn_install))
            }
        },
        confirmButton = {
            FilledTonalButton(
                enabled = !apkItem.isPatched,
                onClick = onPatch
            ) {
                Icon(Icons.TwoTone.Build, null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.btn_patch))
            }
        }
    )
}