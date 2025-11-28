package com.htetz.srpatchx.imageloader

import coil3.key.Keyer
import coil3.request.Options
import com.htetz.srpatchx.models.FileItem

class ApkIconKeyer : Keyer<FileItem.Apk> {
    override fun key(
        data: FileItem.Apk,
        options: Options
    ): String? {
        return data.file.path
    }
}