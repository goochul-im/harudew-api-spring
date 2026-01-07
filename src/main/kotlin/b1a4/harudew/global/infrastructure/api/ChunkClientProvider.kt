package b1a4.harudew.global.infrastructure.api

import b1a4.harudew.global.infrastructure.api.dto.ParserRequest
import b1a4.harudew.global.infrastructure.api.dto.ParserResponse
import b1a4.harudew.global.infrastructure.exception.ChunkClientException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ChunkClientProvider(
    @Value("\${parser.model.url}") // application.yml의 설정값 읽기
    private val parserUrl: String,
    restClientBuilder: RestClient.Builder
) : ChunkClientPort {

    private val restClient = restClientBuilder.build()

    override fun chunk(content: String): List<String> {
        val response = try {
            restClient.post()
                .uri(parserUrl)
                .body(ParserRequest(content)) // 자동으로 JSON 변환
                .retrieve()
                .body(ParserResponse::class.java)
        } catch (e: Exception) {
            throw ChunkClientException(mapOf(
                "content" to content
            ))
        }

        return response?.sentences ?: emptyList()
    }
}
