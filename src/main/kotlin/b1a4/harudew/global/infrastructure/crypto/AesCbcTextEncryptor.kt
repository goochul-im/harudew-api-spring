package b1a4.harudew.global.infrastructure.crypto

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 텍스트를 암호화/복호화하는 클래스입니다
 * enc:: 로 시작하는 암호화된 텍스트는 복호화하고, 암호화되지 않은 텍스트는 복호화를 시도하지 않습니다
 * AES 알고리즘을 채택했습니다
 */
@Component
class AesCbcTextEncryptor(
    @Value("\${encrypt.secret}") private val secretKeyHex: String
) : TextEncryptorPort {

    companion object {
        private const val ALGORITHM = "AES/CBC/PKCS5Padding"
        private const val IV_LENGTH = 16
        private const val PREFIX = "enc::"
    }

    private val keySpec = SecretKeySpec(secretKeyHex.hexToBytes(), "AES")

    override fun encrypt(plainText: String): String {
        if (plainText.startsWith(PREFIX)) return plainText

        val iv = ByteArray(IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        return "${PREFIX}${iv.toHex()}:${encrypted.toHex()}"
    }

    override fun decrypt(cipherText: String): String {
        if (!cipherText.startsWith(PREFIX)) return cipherText

        return try {
            val parts = cipherText.removePrefix(PREFIX).split(":", limit = 2)
            val iv = parts[0].hexToBytes()
            val data = parts[1].hexToBytes()

            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))
            String(cipher.doFinal(data), Charsets.UTF_8)
        } catch (e: Exception) {
            cipherText
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
    private fun String.hexToBytes(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
