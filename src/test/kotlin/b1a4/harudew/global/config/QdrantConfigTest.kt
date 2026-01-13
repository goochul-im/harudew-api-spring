package b1a4.harudew.global.config

import b1a4.harudew.annotation.IntegrationTest
import io.qdrant.client.QdrantClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@IntegrationTest
@SpringBootTest
class QdrantConfigTest {

    @Autowired
    private lateinit var qdrantClient: QdrantClient

    @Test
    @DisplayName("Qdrant 서버와 연결하여 컬렉션 목록을 조회한다")
    fun `Qdrant 연결 및 컬렉션 조회 테스트`() {
        // when
        // 비동기 호출 후 결과를 동기적으로 대기 (get())
        val collections = qdrantClient.listCollectionsAsync().get()

        // then
        assertThat(collections).isNotNull
        println("Successfully connected to Qdrant. Collections found: ${collections.size}")
        collections.forEach { println("- Collection: ${it}") }
    }
}
