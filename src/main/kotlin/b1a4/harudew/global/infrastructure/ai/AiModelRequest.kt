package b1a4.harudew.global.infrastructure.ai

data class AiModelRequest(
    val modelId: String,
    val temperature: Double? = null,
    val topP: Double? = null,
    val topK: Int? = null,
    val maxTokens: Int? = null
)
