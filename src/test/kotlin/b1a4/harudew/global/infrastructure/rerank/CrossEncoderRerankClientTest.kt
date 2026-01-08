package b1a4.harudew.global.infrastructure.rerank

import b1a4.harudew.annotation.IntegrationTest
import b1a4.harudew.global.infrastructure.rerank.CandidateRequest
import b1a4.harudew.global.infrastructure.rerank.CrossEncoderRerankClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@IntegrationTest
class CrossEncoderRerankClientTest {

    @Autowired
    private lateinit var rerankClient: CrossEncoderRerankClient

    @Test
    fun `리랭크 요청을 보내면 점수가 포함된 결과를 반환한다`() {
        // given
        val query = "행복한 하루"
        val candidates = listOf(
            CandidateRequest(1L, "오늘 정말 즐거웠어"),
            CandidateRequest(2L, "너무 슬픈 날이야"),
            CandidateRequest(3L, "그저 그런 평범한 하루였어")
        )

        // when
        val results = rerankClient.fetchRerank(query, candidates)

        println("results : $results")
        // then
        assertThat(results).isNotNull
        assertThat(results).hasSize(3)
        
        // 점수가 높은 순서대로 정렬되어 있는지, 혹은 적어도 결과가 제대로 매핑되었는지 확인
        // (CrossEncoder 구현에 따라 정렬 여부는 다를 수 있으나, 보통 점수를 반환함)
        results.forEach { result ->
            assertThat(result.score).isNotNull()
            assertThat(result.text).isIn(candidates.map { it.text })
        }
        
        val happyResult = results.find { it.id == 1L }
        val sadResult = results.find { it.id == 2L }
        
        if (happyResult != null && sadResult != null) {
            assertThat(happyResult.score).isGreaterThan(sadResult.score)
        }
    }
}
