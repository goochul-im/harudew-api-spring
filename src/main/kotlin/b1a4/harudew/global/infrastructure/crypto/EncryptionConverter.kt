package b1a4.harudew.global.infrastructure.crypto

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class EncryptionConverter(
    private val encryptor: TextEncryptorPort
) : AttributeConverter<String, String> {

    override fun convertToDatabaseColumn(attribute: String): String =
        encryptor.encrypt(attribute)

    override fun convertToEntityAttribute(dbData: String): String =
        encryptor.decrypt(dbData)

}
