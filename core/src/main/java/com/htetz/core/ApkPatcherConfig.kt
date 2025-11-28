package com.htetz.core

object ApkPatcherConfig {
    // File paths
    const val MANIFEST_PATH = "AndroidManifest.xml"
    const val ORIGINAL_APK_PATH = "assets/base.apk"
    const val CONFIG_PATH = "assets/patch/SRPatch_config.json"
    const val LIB_PATH = "assets/patch/lib/arm64-v8a/libSRPatch.so"
    const val RESOURCES_ARSC = "resources.arsc"

    // Alignment settings
    const val PAGE_ALIGNMENT = 4096  // 4KB page alignment for .so files
    const val RESOURCES_ALIGNMENT = 4  // 4-byte alignment for resources.arsc (Android 11+)

    // Patch settings
    const val DEFAULT_APP_NAME = "com.srp.patch.Init"
    const val MIN_SDK_VERSION = 28
}