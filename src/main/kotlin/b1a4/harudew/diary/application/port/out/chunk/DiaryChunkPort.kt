package b1a4.harudew.diary.application.port.out.chunk

interface DiaryChunkPort {

    fun chunk(content: String): ChunkResult

}
