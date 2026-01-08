package b1a4.harudew.diary.application.port.out

import b1a4.harudew.diary.application.port.out.dto.ChunkResult

interface DiaryChunkPort {

    fun chunk(content: String): ChunkResult

}
