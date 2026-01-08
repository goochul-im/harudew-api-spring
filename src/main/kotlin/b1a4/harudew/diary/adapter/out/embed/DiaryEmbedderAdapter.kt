package b1a4.harudew.diary.adapter.out.embed

import b1a4.harudew.diary.application.port.out.DiaryEmbedderPort
import b1a4.harudew.global.infrastructure.embed.DualEmbedClientPort
import org.springframework.stereotype.Component

@Component
class DiaryEmbedderAdapter(
    private val embedClient: DualEmbedClientPort
) : DiaryEmbedderPort {

    override fun embed(content: String): List<Double> {
        return embedClient.embed(content)
    }

}
