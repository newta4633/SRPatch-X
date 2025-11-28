package com.htetz.srpatchx

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.htetz.srpatchx.imageloader.ApkIconFetcher
import com.htetz.srpatchx.imageloader.ApkIconKeyer

class SRPatchXApplication : Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(ApkIconFetcher.Factory(context))
                add(ApkIconKeyer())
            }
            .crossfade(true)
            .apply {
                if (BuildConfig.DEBUG) logger(DebugLogger())
            }
            .build()
    }
}
