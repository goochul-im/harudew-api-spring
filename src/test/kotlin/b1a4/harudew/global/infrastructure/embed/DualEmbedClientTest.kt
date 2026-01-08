package b1a4.harudew.global.infrastructure.embed

import b1a4.harudew.annotation.IntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@IntegrationTest
class DualEmbedClientTest {

    @Autowired
    private lateinit var dualEmbedClient: DualEmbedClient

    @Test
    fun `임베딩 요청을 보내면 벡터값을 반환한다`() {
        // given
        val content = "오늘 하루는 정말 기분이 좋았어."

        // when
        val result = dualEmbedClient.embed(content)

        println(result)

        // then
        assertThat(result).isNotNull
        assertThat(result).isNotEmpty
        assertThat(result.size).isGreaterThan(0)
    }
}
