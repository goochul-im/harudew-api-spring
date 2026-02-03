package b1a4.harudew.diary.adapter.out.persistence.qdrant

import b1a4.harudew.annotation.IntegrationTest
import b1a4.harudew.diary.application.port.out.vector.ContentVectorWrapper
import b1a4.harudew.diary.application.port.out.vector.keyword.SaveKeywordRequest
import b1a4.harudew.diary.application.port.out.vector.keyword.SearchKeywordQuery
import io.qdrant.client.QdrantClient
import io.qdrant.client.grpc.Collections
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
import org.springframework.context.annotation.Import
import java.time.Duration
import java.util.UUID

@IntegrationTest
@SpringBootTest
@Import(QdrantTestConfig::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 테스트 인스턴스를 클래스당 하나만 생성 -> 기본적으로는 메소드당 하나
class QdrantKeywordVectorAdapterTest {

    @Autowired
    private lateinit var qdrantClient: QdrantClient

    @Autowired
    private lateinit var adapter: QdrantKeywordVectorAdapter

    private val testCollectionName = QdrantTestConfig.TEST_COLLECTION_NAME

    @BeforeAll
    fun setupTestCollection() {
        val collections = qdrantClient.listCollectionsAsync().get()

        if (!collections.contains(testCollectionName)) {
            println("테스트 컬렉션 '$testCollectionName' 생성 중...")

            val vectorParams = VectorParams.newBuilder()
                .setSize(768)
                .setDistance(Distance.Cosine)
                .build()

            qdrantClient.createCollectionAsync(
                testCollectionName,
                vectorParams
            ).get()

            println("테스트 컬렉션 '$testCollectionName' 생성 완료")
        } else {
            println("테스트 컬렉션 '$testCollectionName' 이미 존재함")
        }
    }

    @AfterAll
    fun cleanupTestCollection() {
        println("테스트 컬렉션 '$testCollectionName' 삭제 중...")
        try {
            qdrantClient.deleteCollectionAsync(testCollectionName).get()
            println("테스트 컬렉션 '$testCollectionName' 삭제 완료")
        } catch (e: Exception) {
            println("테스트 컬렉션 삭제 실패: ${e.message}")
        }
    }

    @Test
    @DisplayName("테스트 컬렉션 정보를 조회할 수 있다")
    fun `테스트 컬렉션 정보 조회`() {
        // when
        val collectionInfo = qdrantClient.getCollectionInfoAsync(testCollectionName).get()

        // then
        println("테스트 컬렉션 정보: $collectionInfo")
        assertThat(collectionInfo).isNotNull
    }

    @Test
    @DisplayName("키워드를 Qdrant에 저장하고 검색할 수 있다")
    fun `키워드 저장 및 검색 통합 테스트`() {
        // given
        val testAuthorId = System.currentTimeMillis()
        val testDiaryId = 1L
        val testKeyword = "테스트키워드_${UUID.randomUUID()}"
        val testVector = generateTestVector(768)

        println("테스트 authorId: $testAuthorId")
        println("테스트 keyword: $testKeyword")

        val saveRequest = SaveKeywordRequest(
            keywords = listOf(
                ContentVectorWrapper(
                    content = testKeyword,
                    vector = testVector
                )
            ),
            diaryId = testDiaryId,
            authorId = testAuthorId
        )

        // when - 저장
        adapter.save(saveRequest)

        // then - 비동기 저장이 완료될 때까지 대기 및 검증
        await.atMost(Duration.ofSeconds(5)) // 최대 5초 대기
            .pollInterval(Duration.ofMillis(500)) // 0.5초마다 재시도
            .untilAsserted {
                val searchQuery = SearchKeywordQuery(
                    keyword = testKeyword,
                    diaryId = testDiaryId,
                    vector = testVector,
                    authorId = testAuthorId
                )

                val results = adapter.searchByKeyword(searchQuery)

                // 이 조건이 충족되지 않으면 pollInterval 주기로 계속 재시도함
                assertThat(results).isNotEmpty
                assertThat(results.first().keyword).contains(testKeyword)
            }

        println("저장된 키워드 검색 성공!")
    }

    @Test
    @DisplayName("Qdrant에 테스트 컬렉션이 존재하는지 확인한다")
    fun `테스트 컬렉션 존재 확인`() {
        // when
        val collections = qdrantClient.listCollectionsAsync().get()

        // then
        println("현재 컬렉션 목록: $collections")
        assertThat(collections).contains(testCollectionName)
    }

    private fun generateTestVector(dimension: Int): List<Double> {
        return (1..dimension).map { Math.random() }
    }
}
