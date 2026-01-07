package b1a4.harudew.global.infrastructure.api

import b1a4.harudew.global.infrastructure.api.dto.ParserResponse

interface ChunkClientPort {

    fun chunk(content: String): ParserResponse

}
