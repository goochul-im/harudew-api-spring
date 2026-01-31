package b1a4.harudew.global.infrastructure.storage

data class FileUploadRequest(
    val bytes: ByteArray,
    val contentType: String,
    val originalFilename: String?,
)
