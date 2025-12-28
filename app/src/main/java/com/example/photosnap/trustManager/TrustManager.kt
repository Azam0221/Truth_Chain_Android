package com.example.photosnap.trustManager

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.util.Base64


object TrustManager {

    private const val KEY_ALIAS = "TruthChainIdentity"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    @SuppressLint("MissingPermission")
    suspend fun getWitnessData(context: Context): String{
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val location: Location? = fusedLocationClient.lastLocation.await()

        val lat = location?.latitude ?: 0.0
        val long = location?.longitude ?: 0.0
        val time = System.currentTimeMillis()

        return """{"lat": "$lat", "long": "$long", "time": "$time"}"""
    }

    fun signData(data: ByteArray): String{
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        if(!keyStore.containsAlias(KEY_ALIAS)){
            generateKeyPair()
        }
        val entry = keyStore.getEntry(KEY_ALIAS,null) as? KeyStore.PrivateKeyEntry
        val privateKey = entry?.privateKey ?: return ""

        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey)
        signature.update(data)

        val signatureBytes = signature.sign()
        return Base64.getEncoder().encodeToString(signatureBytes)
    }

    private fun generateKeyPair(){
        val kpg = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )
        val parameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).setDigests(KeyProperties.DIGEST_SHA256)
            .build()

        kpg.initialize(parameterSpec)
        kpg.generateKeyPair()
    }

    fun getPublicKey(): String {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val cert = keyStore.getCertificate(KEY_ALIAS)
        return Base64.getEncoder().encodeToString(cert.publicKey.encoded)
    }
}