package b1a4.harudew.diary.adapter.out.persistence.qdrant

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
    fun testKeywordVectorAdapter(
        qdrantClient: QdrantClient,
        uuidGenerator: UuidGenerator
    ): QdrantKeywordVectorAdapter {
        return QdrantKeywordVectorAdapter(
            qdrantClient = qdrantClient,
            uuidGenerator = uuidGenerator,
            collectionName = TEST_COLLECTION_NAME
        )
    }
}
