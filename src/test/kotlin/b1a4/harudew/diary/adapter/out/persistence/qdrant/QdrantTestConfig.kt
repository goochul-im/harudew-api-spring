package b1a4.harudew.diary.adapter.out.persistence.qdrant

import b1a4.harudew.global.infrastructure.qdrant.QdrantClientAdapter
import b1a4.harudew.global.infrastructure.qdrant.QdrantClientPort
import b1a4.harudew.global.util.UuidGenerator
import io.qdrant.client.QdrantClient
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class QdrantTestConfig {

    companion object {
        const val TEST_COLLECTION_NAME = "keyword_test"
    }

    @Bean
    @Primary
    fun testQdrantClientPort(
        qdrantClient: QdrantClient,
        uuidGenerator: UuidGenerator
    ): QdrantClientPort {
        return QdrantClientAdapter(
            qdrantClient = qdrantClient,
            uuidGenerator = uuidGenerator
        )
    }

    @Bean
    @Primary
    fun testKeywordVectorAdapter(
        qdrantClientPort: QdrantClientPort
    ): QdrantKeywordVectorAdapter {
        return QdrantKeywordVectorAdapter(
            qdrantClientPort = qdrantClientPort,
            collectionName = TEST_COLLECTION_NAME
        )
    }
}
