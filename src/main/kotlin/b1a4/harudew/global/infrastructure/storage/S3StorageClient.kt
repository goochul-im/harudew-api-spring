package b1a4.harudew.global.infrastructure.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.UUID

@Component
class S3StorageClient(
    private val s3Client: S3Client,
    @Value("\${aws.s3.bucket-name}") private val bucketName: String,
    @Value("\${aws.s3.region}") private val region: String
) : StorageClientPort {

    override fun upload(request: FileUploadRequest): String {
        val key = "${UUID.randomUUID()}${getExtension(request.originalFilename)}"

        val putRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(request.contentType)
            .build()

        s3Client.putObject(putRequest, RequestBody.fromBytes(request.bytes))

        return "https://${bucketName}.s3.${region}.amazonaws.com/${key}"
    }

    override fun delete(key: String) {
        val deleteRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build()

        s3Client.deleteObject(deleteRequest)
    }

    private fun getExtension(filename: String?): String {
        return filename?.let {
            val dotIndex = it.lastIndexOf('.')
            if (dotIndex >= 0) it.substring(dotIndex) else ""
        } ?: ""
    }
}
