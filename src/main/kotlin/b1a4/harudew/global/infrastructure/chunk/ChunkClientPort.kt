package b1a4.harudew.global.infrastructure.chunk

interface ChunkClientPort {

    fun chunk(content: String): ParserResponse

}
