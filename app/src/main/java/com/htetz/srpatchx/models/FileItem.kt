package com.htetz.srpatchx.models

import androidx.compose.runtime.Immutable
import java.io.File

@Immutable
sealed interface FileItem {
    val file: File

    @Immutable
    data class Regular(override val file: File) : FileItem

    @Immutable
    data class Apk(
        override val file: File,
        val packageName: String,
        val appName: String,
        val versionName: String,
        val versionCode: Long,
        val minSdkVersion: Int,
        val compileSdkVersion: Int,
        val targetSdkVersion: Int,
        val isPatched: Boolean
    ) : FileItem
}