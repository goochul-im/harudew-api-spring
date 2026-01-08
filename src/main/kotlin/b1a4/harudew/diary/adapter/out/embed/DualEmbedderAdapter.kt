package b1a4.harudew.diary.adapter.out.embed

import b1a4.harudew.diary.adapter.exception.EmbeddingFailedException
import b1a4.harudew.diary.application.port.out.DualEmbedderPort
import b1a4.harudew.global.infrastructure.embed.DualEmbedClientPort
import org.springframework.stereotype.Component

@Component
class DualEmbedderAdapter(
    private val embedClient: DualEmbedClientPort
) : DualEmbedderPort {

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
