package com.htetz.srpatchx.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.htetz.core.ApkPatcherConfig
import com.htetz.srpatchx.models.FileItem
import java.io.File

object ApkUtils {
    fun installApk(context: Context, apkFile: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", apkFile)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        }
        context.startActivity(intent)
    }

    fun uninstallApp(context: Context, pkg: String) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_DELETE).apply {
                    data = "package:$pkg".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (_: Exception) {
            launchAppInfo(context, pkg)
        }
    }

    fun launchAppInfo(context: Context, pkg: String) {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = "package:${pkg}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    fun getApkSign(context: Context, apkFile: File): String? =
        getSignature(context) { pm, flags ->
            pm.getPackageArchiveInfo(apkFile.path, flags)
        }

    fun getAppSign(context: Context, packageName: String): String? =
        getSignature(context) { pm, flags ->
            pm.getPackageInfo(packageName, flags)
        }

    private inline fun getSignature(
        context: Context,
        getPackageInfo: (PackageManager, Int) -> PackageInfo?
    ): String? = runCatching {
        val pm = context.packageManager
        val flags = PackageManager.GET_SIGNING_CERTIFICATES
        getPackageInfo(pm, flags)
            ?.signingInfo
            ?.apkContentsSigners
            ?.firstNotNullOf { it.toByteArray().toHexString() }
    }.getOrNull()

    fun getApkIcon(context: Context, apkFile: File): Drawable? {
        return loadPackageInfo(context, apkFile)
            ?.applicationInfo
            ?.loadIcon(context.packageManager)
    }

    fun getApkInfo(context: Context, apkFile: File): FileItem.Apk? = runCatching {
        val pm = context.packageManager
        val packageInfo = loadPackageInfo(context, apkFile) ?: return null
        val appInfo = packageInfo.applicationInfo ?: return null

        FileItem.Apk(
            file = apkFile,
            packageName = packageInfo.packageName,
            versionName = packageInfo.versionName.orEmpty(),
            versionCode = packageInfo.longVersionCode,
            minSdkVersion = appInfo.minSdkVersion,
            targetSdkVersion = appInfo.targetSdkVersion,
            compileSdkVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                appInfo.compileSdkVersion
            } else 0,
            appName = appInfo.loadLabel(pm).toString(),
            isPatched = appInfo.className == ApkPatcherConfig.DEFAULT_APP_NAME,
        )
    }.getOrNull()

    private fun loadPackageInfo(context: Context, apkFile: File): PackageInfo? = runCatching {
        context.packageManager.getPackageArchiveInfo(
            apkFile.path,
            PackageManager.GET_META_DATA
        )?.apply {
            applicationInfo?.let {
                it.sourceDir = apkFile.path
                it.publicSourceDir = apkFile.path
            }
        }
    }.getOrNull()
}