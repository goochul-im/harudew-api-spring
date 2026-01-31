package b1a4.harudew.global.infrastructure.storage

interface StorageClientPort {

    fun upload(request: FileUploadRequest): String

    fun delete(key: String)

}
