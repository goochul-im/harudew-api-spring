package b1a4.harudew.global.infrastructure.storage

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/upload")
class UploadController(
    private val storageClientPort: StorageClientPort
) {

    @PostMapping("/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(@RequestParam("file") file: MultipartFile): Map<String, String> =
        mapOf("imageUrl" to upload(file))

    @PostMapping("/audios", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadAudios(@RequestParam("audios") files: List<MultipartFile>): Map<String, Any> =
        mapOf(
            "success" to true,
            "urls" to files.map(::upload),
            "message" to "오디오 업로드가 완료되었습니다."
        )

    @PostMapping("/multiple-images", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadMultipleImages(@RequestParam("files") files: List<MultipartFile>): Map<String, List<String>> =
        mapOf("imageUrls" to files.map(::upload))

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@RequestParam key: String) {
        storageClientPort.delete(key)
    }

    private fun upload(file: MultipartFile): String =
        storageClientPort.upload(
            FileUploadRequest(
                bytes = file.bytes,
                contentType = file.contentType ?: "application/octet-stream",
                originalFilename = file.originalFilename
            )
        )
}
