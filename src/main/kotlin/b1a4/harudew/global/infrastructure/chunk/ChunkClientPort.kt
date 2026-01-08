package b1a4.harudew.global.infrastructure.chunk

import b1a4.harudew.diary.application.port.out.dto.ChunkResult

interface ChunkClientPort {

    fun chunk(content: String): ChunkResult

}
