package com.htetz.srpatchx.imageloader

import android.content.Context
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.request.Options
import com.htetz.srpatchx.utils.ApkUtils
import com.htetz.srpatchx.models.FileItem

class ApkIconFetcher(
    private val context: Context,
    private val item: FileItem.Apk,
    private val options: Options,
    private val imageLoader: ImageLoader
) : Fetcher {
    class Factory(
        private val context: Context
    ) : Fetcher.Factory<FileItem.Apk> {
        override fun create(
            data: FileItem.Apk,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return ApkIconFetcher(context, data, options, imageLoader)
        }
    }

    override suspend fun fetch(): FetchResult {
        val icon = ApkUtils.getApkIcon(context, item.file)?.asImage()
            ?: throw IllegalStateException("Cannot load icon")

        return ImageFetchResult(
            image = icon,
            isSampled = false,
            DataSource.DISK
        )
    }
}