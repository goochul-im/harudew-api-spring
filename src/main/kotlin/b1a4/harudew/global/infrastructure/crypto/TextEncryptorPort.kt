package b1a4.harudew.global.infrastructure.crypto

interface TextEncryptorPort {

    fun encrypt(plainText: String): String
    fun decrypt(cipherText: String): String

}
