package b1a4.harudew.diary.adapter.out.embed

import b1a4.harudew.diary.application.port.out.embed.SentenceEmbedderPort
import b1a4.harudew.global.infrastructure.embed.dual.DualEmbedClient
import b1a4.harudew.global.infrastructure.embed.dual.DualEmbedClientPort
import org.springframework.stereotype.Component

@Component
class SentenceEmbedderAdapter(
    private val dualEmbedClient: DualEmbedClientPort
) : SentenceEmbedderPort {

    override fun embedPassage(content: String): List<Double> {
        TODO("Not yet implemented")
    }

    override fun embedQuery(content: String): List<Double> {
        TODO("Not yet implemented")
    }
}
