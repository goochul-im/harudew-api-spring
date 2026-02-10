package b1a4.harudew.diary.adapter.out.chunk

import b1a4.harudew.diary.application.port.out.chunk.ChunkResponse
import b1a4.harudew.diary.application.port.out.chunk.DiaryChunkPort
import b1a4.harudew.global.infrastructure.chunk.ChunkClientPort
import org.springframework.stereotype.Component

@Component
class DiaryChunkAdapter(
    private val chunkClient: ChunkClientPort
) : DiaryChunkPort {

    override fun chunk(content: String): ChunkResponse {
        return chunkClient.chunk(content)
    }

}
