package b1a4.harudew.global.infrastructure.crypto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AesCbcTextEncryptorTest {

    private val secretKeyHex = "0123456789abcdef0123456789abcdef"
    private val encryptor = AesCbcTextEncryptor(secretKeyHex)

    @Test
    fun `평문을 암호화하면 enc 접두사가 붙는다`() {
        // given
        val plainText = "오늘 하루는 좋은 날이었다"

        // when
        val encrypted = encryptor.encrypt(plainText)

        // then
        assertThat(encrypted).startsWith("enc::")
    }

    @Test
    fun `암호화된 텍스트를 복호화하면 원본이 복원된다`() {
        // given
        val plainText = "오늘 하루는 좋은 날이었다"

        // when
        val encrypted = encryptor.encrypt(plainText)
        val decrypted = encryptor.decrypt(encrypted)

        // then
        assertThat(decrypted).isEqualTo(plainText)
    }

    @Test
    fun `이미 암호화된 텍스트는 이중 암호화되지 않는다`() {
        // given
        val plainText = "일기 내용"
        val encrypted = encryptor.encrypt(plainText)

        // when
        val doubleEncrypted = encryptor.encrypt(encrypted)

        // then
        assertThat(doubleEncrypted).isEqualTo(encrypted)
    }

    @Test
    fun `enc 접두사가 없는 텍스트는 복호화 없이 그대로 반환된다`() {
        // given
        val plainText = "암호화되지 않은 텍스트"

        // when
        val result = encryptor.decrypt(plainText)

        // then
        assertThat(result).isEqualTo(plainText)
    }

    @Test
    fun `같은 평문을 두 번 암호화하면 IV가 달라 결과가 다르다`() {
        // given
        val plainText = "같은 내용"

        // when
        val first = encryptor.encrypt(plainText)
        val second = encryptor.encrypt(plainText)

        // then
        assertThat(first).isNotEqualTo(second)
        assertThat(encryptor.decrypt(first)).isEqualTo(encryptor.decrypt(second))
    }

    @Test
    fun `빈 문자열도 암호화 및 복호화할 수 있다`() {
        // given
        val plainText = ""

        // when
        val encrypted = encryptor.encrypt(plainText)
        val decrypted = encryptor.decrypt(encrypted)

        // then
        assertThat(encrypted).startsWith("enc::")
        assertThat(decrypted).isEqualTo(plainText)
    }

    @Test
    fun `긴 텍스트도 암호화 및 복호화할 수 있다`() {
        // given
        val plainText = "가".repeat(10_000)

        // when
        val encrypted = encryptor.encrypt(plainText)
        val decrypted = encryptor.decrypt(encrypted)

        // then
        assertThat(decrypted).isEqualTo(plainText)
    }

    @Test
    fun `손상된 암호문은 원본 그대로 반환된다`() {
        // given
        val corrupted = "enc::invalidhexdata:alsobroken"

        // when
        val result = encryptor.decrypt(corrupted)

        // then
        assertThat(result).isEqualTo(corrupted)
    }
}
