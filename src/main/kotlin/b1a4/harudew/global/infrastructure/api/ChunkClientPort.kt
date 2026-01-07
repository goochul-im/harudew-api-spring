package b1a4.harudew.global.infrastructure.api

interface ChunkClientPort {

    fun chunk(content: String): List<String>

}
