package com.htetz.core

import com.android.apksig.ApkVerifier
import com.android.apksig.util.DataSource
import java.security.cert.X509Certificate

internal object SignerCompat {
    fun findAnySign(source: DataSource): String? {
        return try {
            val result = ApkVerifier.Builder(source)
                .setV1SigningEnabled(false)
                .build()
                .verify()
            result.v1SchemeSigners.firstOrNull()?.let {
                return it.certificate.toHexString()
            }

            result.v2SchemeSigners.firstOrNull()?.let {
                return it.certificate.toHexString()
            }

            result.v3SchemeSigners.firstOrNull()?.let {
                return it.certificate.toHexString()
            }

            result.v31SchemeSigners.firstOrNull()?.let {
                return it.certificate.toHexString()
            }

            result.v4SchemeSigners.firstOrNull()?.let {
                return it.certificate.toHexString()
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun X509Certificate.toHexString(): String = this.encoded.toHexString()
}
