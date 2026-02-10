package b1a4.harudew.global.infrastructure.chunk

import b1a4.harudew.diary.application.port.out.chunk.ChunkResponse

interface ChunkClientPort {

    fun chunk(content: String): ChunkResponse

}
