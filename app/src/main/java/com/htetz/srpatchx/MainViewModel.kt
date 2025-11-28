package com.htetz.srpatchx

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.compose.runtime.Immutable
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.htetz.core.ApkPatchOptions
import com.htetz.core.ApkPatcher
import com.htetz.srpatchx.utils.ApkUtils
import com.htetz.srpatchx.domain.isApkFile
import com.htetz.srpatchx.domain.runWithMinimumDelay
import com.htetz.srpatchx.models.FileItem
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.IllegalArgumentException

sealed interface MainUiEvent {
    sealed interface Patch : MainUiEvent {
        data class Success(val apk: FileItem.Apk) : Patch
    }

    data class ShowSnack(val message: String) : MainUiEvent
}

sealed interface MainUiAction {
    sealed interface Dialog : MainUiAction {
        data object Hide : Dialog
        data class Toggle(val state: MainUiDialogState) : Dialog
    }

    sealed class StoragePermission : MainUiAction {
        data object Check : StoragePermission()
    }

    sealed class FileAction : MainUiAction {
        data class Refresh(val showDelay: Boolean = true) : FileAction()
        data object NavigateUp : FileAction()
        data class Click(val item: FileItem) : FileAction()
        data class Delete(val item: FileItem) : FileAction()
    }

    sealed interface Apk : MainUiAction {
        data class Patch(val apk: FileItem.Apk, val options: ApkPatchOptions) : Apk
        data class Install(val item: FileItem.Apk) : Apk
        data class Uninstall(val item: FileItem.Apk) : Apk
    }
}

sealed interface MainUiDialogState {
    data object None : MainUiDialogState
    data object Loading : MainUiDialogState
    data object About : MainUiDialogState
    data class Error(val throwable: Throwable, val title: String? = null) : MainUiDialogState
    sealed interface Apk : MainUiDialogState {
        data class Open(val apk: FileItem.Apk) : Apk
        data class Patch(val apk: FileItem.Apk) : Apk
        data class SignMismatch(val apk: FileItem.Apk) : Apk
    }

    data class Delete(val file: FileItem) : MainUiDialogState
    data object RequestStoragePermission : MainUiDialogState
}

@Immutable
data class MainUiState(
    val dialogState: MainUiDialogState = MainUiDialogState.None,
    val isRefreshing: Boolean = false,
    val currentDirectory: File? = null,
    val files: PersistentList<FileItem> = persistentListOf(),
    val patchOptions: ApkPatchOptions = ApkPatchOptions.DEFAULT
) {
    val canGoBack get() = currentDirectory?.path != Environment.getExternalStorageDirectory().path
}

class MainViewModel(private val application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<MainUiEvent>()
    val events = _events.receiveAsFlow()
    private var currentJob: Job? = null

    init {
        updateStoragePermissionStatus()
        loadFiles(Environment.getExternalStorageDirectory())
    }

    private fun fileRefresh(showDelay: Boolean = true) {
        _uiState.value.currentDirectory?.let { loadFiles(it, showDelay) }
    }

    private fun fileNavigateUp() {
        val currentDirectory = _uiState.value.currentDirectory?.parentFile ?: return
        loadFiles(currentDirectory)
    }

    private fun loadFiles(directory: File, showDelay: Boolean = false) {
        if (currentJob?.isActive == true) return
        currentJob = viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, currentDirectory = directory) }
            runCatching {
                withContext(Dispatchers.IO) {
                    runWithMinimumDelay(if (showDelay) 1000 else 0) {
                        val files = directory.listFiles()?.sortedWith(
                            compareBy<File> { !it.isDirectory }.thenBy { it.name }
                        ) ?: emptyList()

                        files.map { file ->
                            if (file.isApkFile()) {
                                ApkUtils.getApkInfo(application, file) ?: FileItem.Regular(file)
                            } else {
                                FileItem.Regular(file)
                            }
                        }.toPersistentList()
                    }
                }
            }.onSuccess { files ->
                _uiState.update { it.copy(files = files, isRefreshing = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        dialogState = MainUiDialogState.Error(throwable)
                    )
                }
            }
        }
    }

    private fun handleFile(fileItem: FileItem) {
        when (fileItem) {
            is FileItem.Apk -> updateDialogState(MainUiDialogState.Apk.Open(fileItem))
            is FileItem.Regular -> fileItem.file.let { file ->
                if (file.isDirectory) {
                    loadFiles(file)
                }
            }
        }
    }

    private fun updateStoragePermissionStatus() {
        val dialogState = if (isStoragePermissionGranted())
            MainUiDialogState.None
        else MainUiDialogState.RequestStoragePermission
        updateDialogState(dialogState)
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                application, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun onAction(action: MainUiAction) {
        when (action) {
            is MainUiAction.Dialog.Hide -> updateDialogState(MainUiDialogState.None)
            is MainUiAction.Dialog.Toggle -> updateDialogState(action.state)
            is MainUiAction.FileAction -> handleFileAction(action)
            is MainUiAction.StoragePermission.Check -> {
                updateStoragePermissionStatus()
                fileRefresh()
            }

            is MainUiAction.Apk.Patch -> handlePatch(action.apk, action.options)
            is MainUiAction.Apk.Install -> handleInstall(action.item)
            is MainUiAction.Apk.Uninstall -> handleUninstall(action.item)
        }
    }

    private fun handleUninstall(apkItem: FileItem.Apk) {
        updateDialogState(MainUiDialogState.None)
        ApkUtils.uninstallApp(
            application,
            apkItem.packageName
        )
    }

    private fun handleInstall(apkItem: FileItem.Apk) {
        viewModelScope.launch {
            val apkFile = apkItem.file
            val packageName = apkItem.packageName

            val apkSign = ApkUtils.getApkSign(application, apkFile)

            if (apkSign == null) {
                updateDialogState(
                    MainUiDialogState.Error(
                        throwable = IllegalArgumentException(application.getString(R.string.apk_no_sign_msg)),
                        title = application.getString(R.string.apk_no_sign_title)
                    )
                )
                return@launch
            }

            ApkUtils.getAppSign(application, packageName)?.let { appSign ->
                if (appSign != apkSign) {
                    updateDialogState(MainUiDialogState.Apk.SignMismatch(apkItem))
                    return@launch
                }
            }

            updateDialogState(MainUiDialogState.None)
            ApkUtils.installApk(application, apkFile)
        }
    }

    private fun handlePatch(apkItem: FileItem.Apk, options: ApkPatchOptions) {
        viewModelScope.launch {
            updateDialogState(MainUiDialogState.Loading)
            runCatching {
                withContext(Dispatchers.IO) {
                    val inApk = apkItem.file
                    val outApk = File(
                        inApk.parentFile,
                        application.getString(
                            R.string.output_name_format,
                            inApk.nameWithoutExtension,
                            inApk.extension
                        )
                    ).also {
                        if (it.exists()) it.delete()
                    }

                    ApkPatcher(
                        sourceApk = inApk,
                        outputApk = outApk,
                        options = options,
                        onProgress = {
                            println(it)
                        }
                    ).patch()
                    ApkUtils.getApkInfo(application, outApk)!!
                }
            }.onSuccess { apk ->
                fileRefresh(false)
                updateDialogState(MainUiDialogState.None)
                _events.trySend(MainUiEvent.Patch.Success(apk))
            }.onFailure {
                updateDialogState(MainUiDialogState.Error(it))
            }
        }
    }

    private fun handleFileAction(action: MainUiAction.FileAction) {
        when (action) {
            is MainUiAction.FileAction.Click -> handleFile(action.item)
            MainUiAction.FileAction.NavigateUp -> fileNavigateUp()
            is MainUiAction.FileAction.Refresh -> fileRefresh(action.showDelay)
            is MainUiAction.FileAction.Delete -> handleFileDelete(action.item)
        }
    }

    private fun handleFileDelete(item: FileItem) {
        viewModelScope.launch {
            updateDialogState(MainUiDialogState.Loading)
            runCatching {
                withContext(Dispatchers.IO) {
                    item.file.let { file ->
                        if (file.isFile) file.delete()
                        else file.deleteRecursively()
                    }
                }
            }.onSuccess {
                fileRefresh(false)
                updateDialogState(MainUiDialogState.None)
                _events.trySend(MainUiEvent.ShowSnack(application.getString(R.string.deleted_msg)))
            }.onFailure {
                updateDialogState(MainUiDialogState.Error(it))
            }
        }
    }

    private fun updateDialogState(state: MainUiDialogState) {
        _uiState.update { it.copy(dialogState = state) }
    }
}