package b1a4.harudew.diary.adapter.out.embed

import b1a4.harudew.diary.adapter.exception.EmbeddingFailedException
import b1a4.harudew.diary.application.port.out.embed.KeywordEmbedderPort
import b1a4.harudew.global.infrastructure.embed.EmbedClientPort
import org.springframework.stereotype.Component

@Component
class KeywordEmbedderAdapter(
    private val embedClient: EmbedClientPort
) : KeywordEmbedderPort {

    override fun embed(content: String): List<Double> {
        try {
            return embedClient.embed(content)
        } catch (e: Exception) {
            throw EmbeddingFailedException(
                e,
                mapOf(
                    "content" to content
                )
            )
        }
    }

}
