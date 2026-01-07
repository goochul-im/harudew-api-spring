package b1a4.harudew.diary.adapter.out.api

import b1a4.harudew.diary.application.port.out.DiaryChunkPort
import b1a4.harudew.global.infrastructure.api.ChunkClientPort

class DiaryChunkAdapter(
    private val chunkClient: ChunkClientPort
) : DiaryChunkPort {

    override fun chunk(content: String): List<String> {
        return chunkClient.chunk(content).sentences
    }

}
