package b1a4.harudew.diary.adapter.out.embed

import b1a4.harudew.diary.adapter.exception.EmbeddingFailedException
import b1a4.harudew.global.infrastructure.embed.EmbedClientPort
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class SimpleEmbedderAdapterTest {

    @Mock
    lateinit var embedClient: EmbedClientPort

    @InjectMocks
    lateinit var simpleEmbedderAdapter: KeywordEmbedderAdapter

    @Test
    @DisplayName("임베딩 요청 시 클라이언트를 호출하여 결과를 반환한다")
    fun `임베딩 성공 테스트`() {
        // given
        val content = "테스트 내용"
        val expectedEmbedding = listOf(0.1, 0.2, 0.3)

        `when`(embedClient.embed(content)).thenReturn(expectedEmbedding)

        // when
        val result = simpleEmbedderAdapter.embed(content)

        // then
        assertThat(result).isEqualTo(expectedEmbedding)
    }

    @Test
    @DisplayName("클라이언트 호출 중 예외 발생 시 EmbeddingFailedException을 던진다")
    fun `임베딩 실패 시 예외 발생 테스트`() {
        // given
        val content = "테스트 내용"
        val exception = RuntimeException("API Error")

        `when`(embedClient.embed(content)).thenThrow(exception)

        // when & then
        assertThatThrownBy { simpleEmbedderAdapter.embed(content) }
            .isInstanceOf(EmbeddingFailedException::class.java)
            .hasCause(exception)
    }

}

