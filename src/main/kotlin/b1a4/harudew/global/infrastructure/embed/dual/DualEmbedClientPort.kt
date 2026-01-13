package b1a4.harudew.global.infrastructure.embed.dual

import io.netty.util.concurrent.Promise

interface DualEmbedClientPort {

    fun embedQuery(text: String) : List<Double>

    fun embedPassage(text: String) : List<Double>

}
