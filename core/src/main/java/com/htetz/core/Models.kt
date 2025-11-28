package com.htetz.core

import kotlinx.serialization.Serializable
import java.io.InputStream

enum class PMSProxyMethod(val title: String, val description: String) {
    BINDER_PROXY("IBINDER_PROXY", "Proxy through Binder interface"),
    CREATOR_PROXY("CREATOR_PROXY", "Proxy through Parcelable Creator")
}

enum class SignatureStrength(val level: Int, val title: String, val description: String) {
    PMS_HOOK(1, "PMS Hook", "Basic Package Manager protection"),
    IO_PMS_HOOK(2, "IO Hook + PMS Hook", "File system and package manager"),
    MORE_HOOK(3, "More Hook", "Additional hooking techniques"),
    SVC_HOOK(4, "SVC Hook", "System call level protection")
}

data class ApkPatchOptions(
    val pathRedirectionEnabled: Boolean,
    val pmsProxyMethod: PMSProxyMethod,
    val signatureStrength: SignatureStrength,
    val keystoreConfig: KeystoreConfig
) {
    companion object {
        val DEFAULT = ApkPatchOptions(
            pathRedirectionEnabled = false,
            pmsProxyMethod = PMSProxyMethod.BINDER_PROXY,
            signatureStrength = SignatureStrength.SVC_HOOK,
            keystoreConfig = KeystoreConfig.DEFAULT
        )
    }
}

@Serializable
data class SRPatchConfig(
    val apkSize: Long,
    val originalApplicationName: String?,
    val packageName: String,
    val pathRedirectionEnabled: Boolean,
    val pmsProxyMethod: String,
    val signature: String,
    val signatureStrength: Int
)

sealed class KeyStoreSource {
    data class FromFile(val path: String) : KeyStoreSource()
    data class FromResource(val name: String) : KeyStoreSource()

    fun getStream(): InputStream = when (this) {
        is FromFile -> java.io.File(path).inputStream()
        is FromResource -> javaClass.getResourceAsStream("/$name")
    }
}

data class KeystoreConfig(
    val storeSource: KeyStoreSource,
    val keystorePassword: String,
    val keyAlias: String,
    val keyPassword: String,
) {
    companion object {
        val DEFAULT = KeystoreConfig(
            storeSource = KeyStoreSource.FromResource("keystore.jks"),
            keystorePassword = "12345678",
            keyAlias = "patcher",
            keyPassword = "12345678"
        )
    }
}