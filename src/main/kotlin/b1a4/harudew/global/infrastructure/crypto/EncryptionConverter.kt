package b1a4.harudew.global.infrastructure.crypto

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * 엔티티가 저장될 때 자동으로 암호화/복호화를 수행합니다
 */
@Converter
class EncryptionConverter(
    private val encryptor: TextEncryptorPort
) : AttributeConverter<String, String> {

    override fun convertToDatabaseColumn(attribute: String): String =
        encryptor.encrypt(attribute)

    override fun convertToEntityAttribute(dbData: String): String =
        encryptor.decrypt(dbData)

}
