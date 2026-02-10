package b1a4.harudew.global.infrastructure.qdrant

import b1a4.harudew.annotation.IntegrationTest
import b1a4.harudew.global.config.QdrantConfig
import b1a4.harudew.global.util.UuidGenerator
import io.qdrant.client.QdrantClient
import io.qdrant.client.grpc.Collections.Distance
import io.qdrant.client.grpc.Collections.VectorParams
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import java.time.Duration
import java.util.UUID

@IntegrationTest
@SpringBootTest(classes = [QdrantClientAdapterIntegrationTest.TestConfig::class])
@Import(QdrantConfig::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QdrantClientAdapterIntegrationTest {

    @org.springframework.boot.test.context.TestConfiguration
    class TestConfig {
        @Bean
        fun uuidGenerator(): UuidGenerator = object : UuidGenerator {
            override fun generate(): UUID = UUID.randomUUID()
        }

        @Bean
        fun qdrantClientAdapter(
            qdrantClient: QdrantClient,
            uuidGenerator: UuidGenerator
        ): QdrantClientAdapter = QdrantClientAdapter(qdrantClient, uuidGenerator)
    }

    @Autowired
    private lateinit var qdrantClient: QdrantClient

    @Autowired
    private lateinit var adapter: QdrantClientAdapter

    companion object {
        private const val TEST_COLLECTION_NAME = "qdrant_client_adapter_test"
        private const val VECTOR_SIZE = 768
    }

    @BeforeAll
    fun setup() {
        // 테스트 컬렉션 생성
        val collections = qdrantClient.listCollectionsAsync().get()
        if (!collections.contains(TEST_COLLECTION_NAME)) {
            println("테스트 컬렉션 '$TEST_COLLECTION_NAME' 생성 중...")
            val vectorParams = VectorParams.newBuilder()
                .setSize(VECTOR_SIZE.toLong())
                .setDistance(Distance.Cosine)
                .build()

            qdrantClient.createCollectionAsync(TEST_COLLECTION_NAME, vectorParams).get()
            println("테스트 컬렉션 '$TEST_COLLECTION_NAME' 생성 완료")
        }
    }

    @AfterAll
    fun cleanup() {
        // 테스트 컬렉션 삭제
        println("테스트 컬렉션 '$TEST_COLLECTION_NAME' 삭제 중...")
        try {
            qdrantClient.deleteCollectionAsync(TEST_COLLECTION_NAME).get()
            println("테스트 컬렉션 '$TEST_COLLECTION_NAME' 삭제 완료")
        } catch (e: Exception) {
            println("테스트 컬렉션 삭제 실패: ${e.message}")
        }
    }

    @Test
    @DisplayName("포인트를 upsert하고 search로 조회할 수 있다")
    fun `upsert and search integration test`() {
        // given
        val testMemberId = System.currentTimeMillis()
        val testDiaryId = 1L
        val testContent = "테스트 문장_${UUID.randomUUID()}"
        val testVector = generateTestVector(VECTOR_SIZE)

        val points = listOf(
            QdrantPointData(
                vector = testVector,
                payload = mapOf(
                    "memberId" to testMemberId,
                    "diaryId" to testDiaryId,
                    "content" to testContent
                )
            )
        )

        // when - upsert
        adapter.upsertAsync(TEST_COLLECTION_NAME, points)

        // then - 비동기 저장 완료 대기 후 search로 검증
        await.atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted {
                val searchQuery = QdrantSearchQuery(
                    collectionName = TEST_COLLECTION_NAME,
                    vector = testVector,
                    filters = mapOf("memberId" to testMemberId),
                    limit = 10
                )

                val results = adapter.search(searchQuery)

                assertThat(results).isNotEmpty
                assertThat(results.first().getString("content")).isEqualTo(testContent)
                assertThat(results.first().getLong("diaryId")).isEqualTo(testDiaryId)
                assertThat(results.first().score).isGreaterThan(0.99f) // 동일 벡터이므로 유사도 높음
            }

        println("upsert 및 search 테스트 성공!")
    }

    @Test
    @DisplayName("deleteByFilter로 특정 조건의 포인트를 삭제할 수 있다")
    fun `deleteByFilter integration test`() {
        // given - 먼저 데이터 삽입
        val testMemberId = System.currentTimeMillis() + 1000 // 다른 테스트와 구분
        val testDiaryId = 999L
        val testVector = generateTestVector(VECTOR_SIZE)

        val points = listOf(
            QdrantPointData(
                vector = testVector,
                payload = mapOf(
                    "memberId" to testMemberId,
                    "diaryId" to testDiaryId,
                    "content" to "삭제될 데이터"
                )
            )
        )

        adapter.upsertAsync(TEST_COLLECTION_NAME, points)

        // 데이터가 저장될 때까지 대기
        await.atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted {
                val searchQuery = QdrantSearchQuery(
                    collectionName = TEST_COLLECTION_NAME,
                    vector = testVector,
                    filters = mapOf("memberId" to testMemberId),
                    limit = 10
                )
                val results = adapter.search(searchQuery)
                assertThat(results).isNotEmpty
            }

        println("삭제 전 데이터 확인 완료")

        // when - deleteByFilter 실행
        adapter.deleteByFilter(TEST_COLLECTION_NAME, mapOf("memberId" to testMemberId))

        // then - 삭제 확인
        await.atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted {
                val searchQuery = QdrantSearchQuery(
                    collectionName = TEST_COLLECTION_NAME,
                    vector = testVector,
                    filters = mapOf("memberId" to testMemberId),
                    limit = 10
                )
                val results = adapter.search(searchQuery)
                assertThat(results).isEmpty()
            }

        println("deleteByFilter 테스트 성공 - 데이터가 삭제됨!")
    }

    @Test
    @DisplayName("빈 포인트 리스트로 upsert해도 에러가 발생하지 않는다")
    fun `upsert with empty list does not throw`() {
        // when & then - 에러 없이 완료되어야 함
        adapter.upsertAsync(TEST_COLLECTION_NAME, emptyList())
        println("빈 리스트 upsert 테스트 성공!")
    }

    @Test
    @DisplayName("scoreThreshold를 설정하면 해당 점수 이상의 결과만 반환된다")
    fun `search with scoreThreshold filters low score results`() {
        // given
        val testMemberId = System.currentTimeMillis() + 2000
        val testVector = generateTestVector(VECTOR_SIZE)
        val differentVector = generateTestVector(VECTOR_SIZE) // 다른 벡터

        val points = listOf(
            QdrantPointData(
                vector = testVector,
                payload = mapOf(
                    "memberId" to testMemberId,
                    "content" to "원본 벡터 데이터"
                )
            )
        )

        adapter.upsertAsync(TEST_COLLECTION_NAME, points)

        await.atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted {
                val searchQuery = QdrantSearchQuery(
                    collectionName = TEST_COLLECTION_NAME,
                    vector = testVector,
                    filters = mapOf("memberId" to testMemberId),
                    limit = 10
                )
                val results = adapter.search(searchQuery)
                assertThat(results).isNotEmpty
            }

        // when - 높은 scoreThreshold로 다른 벡터 검색
        val searchQueryWithHighThreshold = QdrantSearchQuery(
            collectionName = TEST_COLLECTION_NAME,
            vector = differentVector,
            filters = mapOf("memberId" to testMemberId),
            limit = 10,
            scoreThreshold = 0.99f // 매우 높은 임계값
        )

        val results = adapter.search(searchQueryWithHighThreshold)

        // then - 다른 벡터로 검색하면 높은 임계값 때문에 결과 없음
        assertThat(results).isEmpty()
        println("scoreThreshold 테스트 성공!")

        // cleanup
        adapter.deleteByFilter(TEST_COLLECTION_NAME, mapOf("memberId" to testMemberId))
    }

    private fun generateTestVector(dimension: Int): List<Double> {
        return (1..dimension).map { Math.random() }
    }
}
