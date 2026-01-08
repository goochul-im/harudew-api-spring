package b1a4.harudew.global.infrastructure.embed

data class EmbedResponse(
    val success: Boolean,
    val embedding: List<Double>,
    val dimension: Int?,
    val error: String?
)
