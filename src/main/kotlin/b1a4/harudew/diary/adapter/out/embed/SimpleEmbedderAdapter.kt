package b1a4.harudew.diary.adapter.out.embed

import b1a4.harudew.diary.adapter.exception.EmbeddingFailedException
import b1a4.harudew.diary.application.port.out.embed.SimpleEmbedderPort
import b1a4.harudew.global.infrastructure.embed.EmbedClientPort
import org.springframework.stereotype.Component

@Component
class SimpleEmbedderAdapter(
    private val embedClient: EmbedClientPort
) : SimpleEmbedderPort {

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
