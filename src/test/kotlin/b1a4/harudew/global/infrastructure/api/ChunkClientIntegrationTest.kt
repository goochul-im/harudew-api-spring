package b1a4.harudew.global.infrastructure.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@Tag("integration")
class ChunkClientIntegrationTest {

    @Autowired
    private lateinit var chunkClientProvider: ChunkClientProvider

    @Test
    @DisplayName("실제 파서 서버에 요청하여 문장이 정상적으로 분리되는지 확인한다")
    fun chunkIntegrationTest() {
        // given
        val content = "첫 번째 문장입니다.두 번째 문장입니다.세 번째 문장입니다.네 번째 문장입니다.다섯 번째 문장입니다."

        println("요청 내용:\n$content")

        // when
        val result = chunkClientProvider.chunk(content)

        // then
        println("응답 결과: $result")

        assertThat(result).isNotNull
        assertThat(result.sentences).hasSize(5)
        assertThat(result.sentences).containsExactly(
            "첫 번째 문장입니다.",
            "두 번째 문장입니다.",
            "세 번째 문장입니다.",
            "네 번째 문장입니다.",
            "다섯 번째 문장입니다."
        )
    }
}
