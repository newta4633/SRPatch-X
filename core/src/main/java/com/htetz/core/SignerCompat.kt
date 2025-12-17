package com.htetz.core

import com.android.apksig.ApkVerifier
import com.android.apksig.util.DataSource
import java.security.cert.X509Certificate

internal object SignerCompat {
    fun findAnySign(source: DataSource): String? {
        return try {
            // ФИКС 1: Используем setMinCheckedPlatformVersion(28) вместо setV1SigningEnabled.
            // Это заставляет верификатор работать по логике Android 9+,
            // где приоритет отдается V2/V3, а ошибки V1 часто игнорируются.
            val result = ApkVerifier.Builder(source)
                .setMinCheckedPlatformVersion(28)
                .build()
                .verify()

            // ФИКС 2: Заменили .certificate на .certificates.firstOrNull()
            // В старых версиях библиотеки сертификаты хранятся в списке (certificates).
            
            result.v4SchemeSigners.firstOrNull()?.certificates?.firstOrNull()?.let {
                return it.toHexString()
            }
            
            result.v31SchemeSigners.firstOrNull()?.certificates?.firstOrNull()?.let {
                return it.toHexString()
            }

            result.v3SchemeSigners.firstOrNull()?.certificates?.firstOrNull()?.let {
                return it.toHexString()
            }

            result.v2SchemeSigners.firstOrNull()?.certificates?.firstOrNull()?.let {
                return it.toHexString()
            }

            // V1 блок мы специально не проверяем
            
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun X509Certificate.toHexString(): String = this.encoded.toHexString()
}
