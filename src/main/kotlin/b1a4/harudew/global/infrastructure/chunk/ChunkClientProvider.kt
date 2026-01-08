package b1a4.harudew.global.infrastructure.chunk

import b1a4.harudew.diary.application.port.out.dto.ChunkResult
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

    override fun chunk(content: String): ChunkResult {
        val response =
            restClient.post()
                .uri(parserUrl)
                .body(ParserRawRequest(content)) // 자동으로 JSON 변환
                .retrieve()
                .body(ParserRawResponse::class.java)

        return ChunkResult(response!!.sentences)
    }

    private data class ParserRawRequest(
        val text: String
    )

    private data class ParserRawResponse(
        val sentences: List<String>
    )

}
