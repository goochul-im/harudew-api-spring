package b1a4.harudew.global.infrastructure.embed

import b1a4.harudew.global.exception.BusinessException
import b1a4.harudew.global.exception.ErrorCode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class DualEmbedClient(
    @Value("\${embed.dual.url}") // application.yml의 설정값 읽기
    private val url: String,
    restClientBuilder: RestClient.Builder
) : DualEmbedClientPort {

    private val restClient = restClientBuilder.build()

    override fun embed(content: String): List<Double> {
        val cleanText = content.trim()
        if (cleanText.isEmpty()) {
            throw RuntimeException()
        }

        val response = restClient.post()
            .uri(url)
            .body(EmbedRequest(cleanText))
            .retrieve()
            .body(EmbedResponse::class.java)

        return response!!.embedding
    }

}
