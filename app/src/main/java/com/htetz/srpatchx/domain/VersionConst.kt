package com.htetz.srpatchx.domain

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

object VersionConst {
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
    fun atLeastAndroid11(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }
}