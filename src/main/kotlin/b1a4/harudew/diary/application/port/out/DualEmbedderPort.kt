package b1a4.harudew.diary.application.port.out

interface DualEmbedderPort {

    fun embed(content: String): List<Double>

}
