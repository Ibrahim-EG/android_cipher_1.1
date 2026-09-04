package com.example.premiumcipher

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.Normalizer
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoException(message: String, cause: Throwable? = null) : Exception(message, cause)

object CryptoEngine {

    private const val FORMAT_V3 = "CPHR3"
    private const val FORMAT_V2 = "CPHR2"
    private const val FORMAT_V1 = "CPHR1"

    private const val SALT_LEN = 16
    private const val NONCE_LEN = 12
    private const val GCM_TAG_BITS = 128

    private const val LEGACY_SCRYPT_N = 524288
    private const val SECURE_SCRYPT_N = 524288
    private const val SCRYPT_R = 8
    private const val SCRYPT_P = 1

    private val UTF_8 = StandardCharsets.UTF_8

    fun normalizePassphrase(passphrase: String): String {
        return Normalizer.normalize(passphrase, Normalizer.Form.NFKC)
    }

    fun passphraseLength(passphrase: String): Int {
        val normalized = normalizePassphrase(passphrase)
        return Character.codePointCount(normalized, 0, normalized.length)
    }

    fun passphraseEquals(a: String, b: String): Boolean {
        val na = normalizePassphrase(a).toByteArray(UTF_8)
        val nb = normalizePassphrase(b).toByteArray(UTF_8)

        return try {
            MessageDigest.isEqual(na, nb)
        } finally {
            na.fill(0)
            nb.fill(0)
        }
    }

    fun encryptSecure(plaintext: String, passphrase: String): String {
        val salt = ByteArray(SALT_LEN)
        val nonce = ByteArray(NONCE_LEN)

        val random = SecureRandom()
        random.nextBytes(salt)
        random.nextBytes(nonce)

        val plaintextBytes = plaintext.toByteArray(UTF_8)
        val keys = deriveSecureKeys(passphrase, salt)

        return try {
            val aad = FORMAT_V3.toByteArray(UTF_8)
            val ciphertext = aesGcm(keys.encryption, nonce, plaintextBytes, aad, encrypt = true)

            val verifier = hmacSha256(
                keys.verification,
                macPayload(FORMAT_V3, salt, nonce, ciphertext)
            )

            buildString {
                append(FORMAT_V3)
                append('$')
                append(b64Encode(salt))
                append('.')
                append(b64Encode(nonce))
                append('.')
                append(b64Encode(ciphertext))
                append('.')
                append(b64Encode(verifier))
            }
        } catch (e: GeneralSecurityException) {
            throw CryptoException("Encryption failed.", e)
        } finally {
            plaintextBytes.fill(0)
            keys.zero()
        }
    }

    fun decryptAny(token: String, passphrase: String): String {
        val cleaned = token.trim().filterNot { it.isWhitespace() }

        if (cleaned.isEmpty()) {
            throw CryptoException("Empty token.")
        }

        val dollar = cleaned.indexOf('$')
        if (dollar <= 0 || dollar == cleaned.length - 1) {
            throw CryptoException("Malformed token.")
        }

        val tag = cleaned.substring(0, dollar)
        val payload = cleaned.substring(dollar + 1)

        return try {
            when (tag) {
                FORMAT_V3 -> decryptV3(payload, passphrase)
                FORMAT_V2 -> decryptV2(payload, passphrase)
                FORMAT_V1 -> decryptV1(payload, passphrase)
                else -> throw CryptoException("Unsupported format: $tag")
            }
        } catch (e: GeneralSecurityException) {
            throw CryptoException("Invalid passphrase or corrupted data.", e)
        } catch (e: IllegalArgumentException) {
            throw CryptoException("Malformed token.", e)
        }
    }

    private fun decryptV3(payload: String, passphrase: String): String {
        val parts = payload.split('.')
        if (parts.size != 4) {
            throw CryptoException("Malformed CPHR3 payload.")
        }

        val salt = b64Decode(parts[0])
        val nonce = b64Decode(parts[1])
        val ciphertext = b64Decode(parts[2])
        val verifier = b64Decode(parts[3])

        val keys = deriveSecureKeys(passphrase, salt)

        return try {
            val expected = hmacSha256(
                keys.verification,
                macPayload(FORMAT_V3, salt, nonce, ciphertext)
            )

            val ok = MessageDigest.isEqual(expected, verifier)
            expected.fill(0)

            if (!ok) {
                throw CryptoException("Invalid passphrase or corrupted data.")
            }

            val aad = FORMAT_V3.toByteArray(UTF_8)
            val plaintextBytes = aesGcm(keys.encryption, nonce, ciphertext, aad, encrypt = false)

            val text = decodeUtf8Strict(plaintextBytes)
            plaintextBytes.fill(0)
            text
        } catch (e: GeneralSecurityException) {
            throw CryptoException("Invalid passphrase or corrupted data.", e)
        } finally {
            keys.zero()
            verifier.fill(0)
        }
    }

    private fun decryptV2(payload: String, passphrase: String): String {
        val parts = payload.split('.')
        if (parts.size != 4) {
            throw CryptoException("Malformed CPHR2 payload.")
        }

        val salt = b64Decode(parts[0])
        val nonce = b64Decode(parts[1])
        val ciphertext = b64Decode(parts[2])

        val key = deriveLegacyKey(passphrase, salt)

        return try {
            val plaintextBytes = aesGcm(key, nonce, ciphertext, aad = null, encrypt = false)
            val text = decodeUtf8Strict(plaintextBytes)
            plaintextBytes.fill(0)
            text
        } finally {
            key.fill(0)
        }
    }

    private fun decryptV1(payload: String, passphrase: String): String {
        val parts = payload.split('.')
        if (parts.size != 3) {
            throw CryptoException("Malformed CPHR1 payload.")
        }

        val salt = b64Decode(parts[0])
        val nonce = b64Decode(parts[1])
        val ciphertext = b64Decode(parts[2])

        val key = deriveLegacyKey(passphrase, salt)

        return try {
            val plaintextBytes = aesGcm(key, nonce, ciphertext, aad = null, encrypt = false)
            val text = decodeUtf8Strict(plaintextBytes)
            plaintextBytes.fill(0)
            text
        } finally {
            key.fill(0)
        }
    }

    private fun deriveSecureKeys(passphrase: String, salt: ByteArray): DerivedKeys {
        val normalized = normalizePassphrase(passphrase)
        val passBytes = normalized.toByteArray(UTF_8)

        val material = try {
            NativeScrypt.scryptNative(passBytes, salt, SECURE_SCRYPT_N.toLong(), SCRYPT_R, SCRYPT_P, 64)
                ?: throw CryptoException("Native Scrypt engine failed.")
        } finally {
            passBytes.fill(0)
        }

        val encryption = material.copyOfRange(0, 32)
        val verification = material.copyOfRange(32, 64)
        material.fill(0)

        return DerivedKeys(encryption, verification)
    }

    private fun deriveLegacyKey(passphrase: String, salt: ByteArray): ByteArray {
        val passBytes = passphrase.toByteArray(UTF_8)

        return try {
            NativeScrypt.scryptNative(passBytes, salt, LEGACY_SCRYPT_N.toLong(), SCRYPT_R, SCRYPT_P, 32)
                ?: throw CryptoException("Native Scrypt engine failed.")
        } finally {
            passBytes.fill(0)
        }
    }

    private fun aesGcm(
        key: ByteArray,
        nonce: ByteArray,
        input: ByteArray,
        aad: ByteArray?,
        encrypt: Boolean
    ): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val mode = if (encrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE
        val keySpec = SecretKeySpec(key, "AES")

        cipher.init(mode, keySpec, GCMParameterSpec(GCM_TAG_BITS, nonce))

        if (aad != null) {
            cipher.updateAAD(aad)
        }

        return cipher.doFinal(input)
    }

    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(key, "HmacSHA256")
        mac.init(keySpec)
        return mac.doFinal(data)
    }

    private fun macPayload(
        tag: String,
        salt: ByteArray,
        nonce: ByteArray,
        ciphertext: ByteArray
    ): ByteArray {
        val out = ByteArrayOutputStream()

        out.write(tag.toByteArray(UTF_8))
        out.write(0)

        out.write(salt)
        out.write(0)

        out.write(nonce)
        out.write(0)

        out.write(ciphertext)

        return out.toByteArray()
    }

    private fun decodeUtf8Strict(bytes: ByteArray): String {
        val decoder = UTF_8.newDecoder()

        return try {
            decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString()
        } catch (e: java.nio.charset.CharacterCodingException) {
            throw CryptoException("Invalid passphrase or corrupted data.", e)
        }
    }

    private fun b64Encode(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP)
    }

    private fun b64Decode(value: String): ByteArray {
        val padded = when (value.length % 4) {
            2 -> value + "=="
            3 -> value + "="
            else -> value
        }

        return Base64.decode(padded, Base64.URL_SAFE)
    }

    private class DerivedKeys(
        val encryption: ByteArray,
        val verification: ByteArray
    ) {
        fun zero() {
            encryption.fill(0)
            verification.fill(0)
        }
    }
}
