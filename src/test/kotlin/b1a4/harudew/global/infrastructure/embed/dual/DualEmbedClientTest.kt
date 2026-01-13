package b1a4.harudew.global.infrastructure.embed.dual

import b1a4.harudew.annotation.IntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@IntegrationTest
class DualEmbedClientTest {

    @Autowired
    private lateinit var dualEmbedClient: DualEmbedClient

    @Test
    @DisplayName("쿼리 임베딩 API를 호출하여 응답을 받아온다")
    fun `쿼리 임베딩 API 호출 테스트`() {
        // given
        val text = "안녕하세요, 테스트 쿼리입니다."

        // when
        val result = dualEmbedClient.embedQuery(text)

        // then
        assertThat(result).isNotNull
        assertThat(result).isNotEmpty
        println("Query Embedding Result Size: ${result.size}")
    }

    @Test
    @DisplayName("패시지 임베딩 API를 호출하여 응답을 받아온다")
    fun `패시지 임베딩 API 호출 테스트`() {
        // given
        val text = "안녕하세요, 테스트 패시지(문서)입니다. 임베딩이 잘 생성되나요?"

        // when
        val result = dualEmbedClient.embedPassage(text)

        // then
        assertThat(result).isNotNull
        assertThat(result).isNotEmpty
        println("Passage Embedding Result Size: ${result.size}")
    }
}
