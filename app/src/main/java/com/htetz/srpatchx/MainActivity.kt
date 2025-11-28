package com.htetz.srpatchx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.htetz.srpatchx.MainUiAction.Apk.Install
import com.htetz.srpatchx.models.FileItem
import com.htetz.srpatchx.ui.composable.AboutDialog
import com.htetz.srpatchx.ui.composable.ApkOpenDialog
import com.htetz.srpatchx.ui.composable.ApkPatchSheet
import com.htetz.srpatchx.ui.composable.ApkSignMismatchDialog
import com.htetz.srpatchx.ui.composable.ErrorDialog
import com.htetz.srpatchx.ui.composable.FileDeleteConfirmDialog
import com.htetz.srpatchx.ui.composable.FileEmpty
import com.htetz.srpatchx.ui.composable.FileItemCard
import com.htetz.srpatchx.ui.composable.LifecycleResumeObserver
import com.htetz.srpatchx.ui.composable.LoadingDialog
import com.htetz.srpatchx.ui.composable.StoragePermissionDialog
import com.htetz.srpatchx.ui.theme.ReSRPatchTheme
import kotlinx.collections.immutable.PersistentList

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    private val viewModel by viewModels<MainViewModel>()

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReSRPatchTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val onAction = viewModel::onAction
                val context = LocalContext.current

                val snackBarHostState = remember { SnackbarHostState() }

                LaunchedEffect(viewModel) {
                    viewModel.events.collect { event ->
                        when (event) {
                            is MainUiEvent.Patch.Success -> {
                                snackBarHostState.currentSnackbarData?.dismiss()
                                val result = snackBarHostState.showSnackbar(
                                    message = event.apk.file.path,
                                    actionLabel = context.getString(R.string.btn_install),
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Long
                                )

                                if (result == SnackbarResult.ActionPerformed) {
                                    onAction(Install(event.apk))
                                }
                            }

                            is MainUiEvent.ShowSnack -> {
                                snackBarHostState.currentSnackbarData?.dismiss()
                                snackBarHostState.showSnackbar(event.message)
                            }
                        }
                    }
                }

                val scrollBehavior =
                    TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

                BackHandler(uiState.canGoBack) {
                    onAction(MainUiAction.FileAction.NavigateUp)
                }

                LifecycleResumeObserver {
                    onAction(MainUiAction.FileAction.Refresh(false))
                }

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    snackbarHost = { SnackbarHost(snackBarHostState) },
                    topBar = {
                        MediumFlexibleTopAppBar(
                            title = {
                                Text(stringResource(R.string.app_name))
                            },
                            subtitle = {
                                Text("${uiState.currentDirectory?.path}")
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        onAction(MainUiAction.Dialog.Toggle(MainUiDialogState.About))
                                    }
                                ) { Icon(Icons.TwoTone.Info, null) }
                            },
                            scrollBehavior = scrollBehavior
                        )
                    },
                ) { innerPadding ->
                    MainContent(
                        innerPadding = innerPadding,
                        isRefreshing = uiState.isRefreshing,
                        files = uiState.files,
                        onAction = onAction
                    )

                    MainDialog(dialogState = uiState.dialogState, onAction = onAction)
                }
            }
        }
    }
}

@Composable
private fun MainDialog(
    dialogState: MainUiDialogState,
    onAction: (MainUiAction) -> Unit,
) {
    when (val dialogState = dialogState) {
        MainUiDialogState.None -> Unit
        MainUiDialogState.Loading -> LoadingDialog()
        MainUiDialogState.RequestStoragePermission -> StoragePermissionDialog(
            onDismiss = {
                onAction(MainUiAction.Dialog.Hide)
            },
            onChange = {
                onAction(MainUiAction.StoragePermission.Check)
            }
        )

        is MainUiDialogState.Apk -> ApkDialog(
            dialogState = dialogState,
            onAction = onAction
        )

        is MainUiDialogState.Error -> ErrorDialog(
            throwable = dialogState.throwable,
            title = dialogState.title ?: stringResource(R.string.title_error),
            onClose = {
                onAction(MainUiAction.Dialog.Hide)
            }
        )

        is MainUiDialogState.Delete -> FileDeleteConfirmDialog(
            item = dialogState.file,
            onDismiss = {
                onAction(MainUiAction.Dialog.Hide)
            },
            onConfirm = {
                onAction(MainUiAction.FileAction.Delete(dialogState.file))
            }
        )

        is MainUiDialogState.About -> AboutDialog(
            onDismiss = {
                onAction(MainUiAction.Dialog.Hide)
            }
        )
    }
}

@Composable
private fun ApkDialog(
    dialogState: MainUiDialogState.Apk,
    onAction: (MainUiAction) -> Unit,
) {
    when (dialogState) {
        is MainUiDialogState.Apk.Open -> ApkOpenDialog(
            apkItem = dialogState.apk,
            onPatch = {
                onAction(
                    MainUiAction.Dialog.Toggle(
                        MainUiDialogState.Apk.Patch(dialogState.apk)
                    )
                )
            },
            onInstall = {
                onAction(MainUiAction.Apk.Install(dialogState.apk))
            },
            onDismiss = {
                onAction(MainUiAction.Dialog.Hide)
            }
        )

        is MainUiDialogState.Apk.Patch -> ApkPatchSheet(
            apkItem = dialogState.apk,
            onPatch = {
                onAction(MainUiAction.Apk.Patch(dialogState.apk, it))
            },
            onDismiss = {
                onAction(MainUiAction.Dialog.Hide)
            }
        )

        is MainUiDialogState.Apk.SignMismatch -> {
            ApkSignMismatchDialog(
                onDismiss = {
                    onAction(MainUiAction.Dialog.Hide)
                },
                onUninstall = {
                    onAction(MainUiAction.Apk.Uninstall(dialogState.apk))
                }
            )
        }
    }
}

@Composable
private fun MainContent(
    innerPadding: PaddingValues,
    isRefreshing: Boolean,
    files: PersistentList<FileItem>,
    onAction: (MainUiAction) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    PullToRefreshBox(
        modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
        isRefreshing = isRefreshing,
        onRefresh = { onAction(MainUiAction.FileAction.Refresh(true)) }
    ) {
        if (files.isEmpty()) {
            FileEmpty(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    bottom = innerPadding.calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(
                    items = files,
                    key = { it.file.path }
                ) { item ->
                    FileItemCard(
                        modifier = Modifier
                            .animateItem(),
                        item = item,
                        onLongClick = {
                            onAction(
                                MainUiAction.Dialog.Toggle(
                                    MainUiDialogState.Delete(item)
                                )
                            )
                        },
                        onClick = {
                            onAction(MainUiAction.FileAction.Click(item))
                        }
                    )
                }
            }
        }
    }
}