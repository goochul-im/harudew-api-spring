package b1a4.harudew.diary.adapter.out.rerank

import b1a4.harudew.diary.adapter.exception.RerankFailedException
import b1a4.harudew.diary.application.port.out.rerank.Candidates
import b1a4.harudew.diary.application.port.out.rerank.RerankRequest
import b1a4.harudew.diary.application.port.out.rerank.RerankResult
import b1a4.harudew.global.infrastructure.rerank.CandidateRequest
import b1a4.harudew.global.infrastructure.rerank.RerankClientPort
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyList
import org.mockito.kotlin.any
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class RerankAdapterTest {

    @Mock
    private lateinit var rerankClient: RerankClientPort

    private lateinit var rerankAdapter: RerankAdapter

    @BeforeEach
    fun setUp() {
        rerankAdapter = RerankAdapter(rerankClient)
    }

    @Test
    @DisplayName("리랭크 요청 시 클라이언트를 호출하여 결과를 반환한다")
    fun `리랭크 성공 테스트`() {
        // given
        val query = "테스트 쿼리"
        val candidates = listOf(
            Candidates(1L, "첫 번째 문서"),
            Candidates(2L, "두 번째 문서")
        )
        val request = RerankRequest(query, candidates)

        val expectedResults = listOf(
            RerankResult(2L, "두 번째 문서", 0.95),
            RerankResult(1L, "첫 번째 문서", 0.85)
        )

        val candidateRequests = listOf(
            CandidateRequest(1L, "첫 번째 문서"),
            CandidateRequest(2L, "두 번째 문서")
        )

        `when`(rerankClient.fetchRerank(query, candidateRequests))
            .thenReturn(expectedResults)

        // when
        val result = rerankAdapter.rerank(request)

        // then
        assertThat(result).hasSize(2)
        assertThat(result[0].score).isEqualTo(0.95)
        assertThat(result[1].score).isEqualTo(0.85)
    }

    @Test
    @DisplayName("클라이언트 호출 중 예외 발생 시 RerankFailedException을 던진다")
    fun `리랭크 실패 시 예외 발생 테스트`() {
        // given
        val query = "테스트 쿼리"
        val candidates = listOf(Candidates(1L, "문서"))
        val request = RerankRequest(query, candidates)

        `when`(rerankClient.fetchRerank(any(), anyList()))
            .thenThrow(RuntimeException("API Error"))

        // when & then
        assertThatThrownBy { rerankAdapter.rerank(request) }
            .isInstanceOf(RerankFailedException::class.java)
            .hasCauseInstanceOf(RuntimeException::class.java)
    }
}
