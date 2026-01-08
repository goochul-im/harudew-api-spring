package b1a4.harudew.diary.adapter.out.chunk

import b1a4.harudew.diary.application.port.out.DiaryChunkPort
import b1a4.harudew.diary.application.port.out.dto.ChunkResult
import b1a4.harudew.global.infrastructure.chunk.ChunkClientPort
import org.springframework.stereotype.Component

@Component
class DiaryChunkAdapter(
    private val chunkClient: ChunkClientPort
) : DiaryChunkPort {

    override fun chunk(content: String): ChunkResult {
        return chunkClient.chunk(content)
    }

}
