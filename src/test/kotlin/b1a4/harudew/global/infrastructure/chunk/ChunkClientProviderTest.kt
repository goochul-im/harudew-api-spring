package b1a4.harudew.global.infrastructure.chunk

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.HttpServerErrorException

@RestClientTest(ChunkClientProvider::class)
@TestPropertySource(properties = ["parser.model.url=http://test-parser.com/parse"])
class ChunkClientProviderTest {

    @Autowired
    private lateinit var chunkClientProvider: ChunkClientProvider

    @Autowired
    private lateinit var mockServer: MockRestServiceServer

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `5줄짜리 문장을 요청하면 5개의 문장으로 분리되어 반환된다`() {
        // given
        val content = "첫 번째 문장입니다. 두 번째 문장입니다. 세 번째 문장입니다. 네 번째 문장입니다. 다섯 번째 문장입니다."

        // 객체를 생성하지 않고, 실제 Flask 서버가 보낼 JSON 구조를 그대로 작성
        val responseJson = """
        {
            "sentences": [
                "첫 번째 문장입니다.",
                "두 번째 문장입니다.",
                "세 번째 문장입니다.",
                "네 번째 문장입니다.",
                "다섯 번째 문장입니다."
            ]
        }
    """.trimIndent()

        mockServer.expect(requestTo("http://test-parser.com/parse"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        // when
        val result = chunkClientProvider.chunk(content)

        // then
        assertThat(result.chunks).hasSize(5) // result가 List<String>인 경우
        assertThat(result.chunks).containsExactly(
            "첫 번째 문장입니다.",
            "두 번째 문장입니다.",
            "세 번째 문장입니다.",
            "네 번째 문장입니다.",
            "다섯 번째 문장입니다."
        )
    }

    @Test
    fun `외부 서버가 500 에러를 반환하면 예외를 던지거나 null을 반환한다`() {
        // given
        mockServer.expect(requestTo("http://test-parser.com/parse"))
            .andRespond(withServerError()) // 500 Internal Server Error 응답

        // when & then
        assertThrows<HttpServerErrorException> {
            chunkClientProvider.chunk("내용")
        }

    }
}
