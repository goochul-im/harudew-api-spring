package b1a4.harudew.global.infrastructure.storage

import b1a4.harudew.annotation.IntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import software.amazon.awssdk.services.s3.S3Client

@SpringBootTest
@IntegrationTest
class S3StorageClientIntegrationTest {

    @Autowired
    private lateinit var s3StorageClient: S3StorageClient

    @Autowired
    private lateinit var s3Client: S3Client

    @Test
    @DisplayName("S3 연결 테스트")
    fun connectionTest() {
        println("S3 버킷 목록 조회 시작...")

        s3Client.listBuckets().buckets().forEach {
            println("버킷: ${it.name()}")
        }

        println("연결 성공!")
    }

    @Test
    @DisplayName("파일을 업로드하고 삭제한다")
    fun uploadAndDeleteTest() {
        // given
        val testContent = "테스트 파일 내용입니다.".toByteArray()
        val request = FileUploadRequest(
            bytes = testContent,
            contentType = "text/plain",
            originalFilename = "test-file.txt"
        )

        // when - upload
        val uploadedUrl = s3StorageClient.upload(request)

        // then - verify upload
        println("업로드된 URL: $uploadedUrl")
        assertThat(uploadedUrl).isNotBlank()
        assertThat(uploadedUrl).contains(".s3.")
        assertThat(uploadedUrl).contains(".txt")

        // when - delete
        val key = uploadedUrl.substringAfterLast("/")
        println("삭제할 key: $key")
        s3StorageClient.delete(key)

        // then - delete completed without exception
        println("삭제 완료")
    }
}
