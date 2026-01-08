package b1a4.harudew.global.infrastructure.ai

import b1a4.harudew.global.infrastructure.ai.AiModelRequest
import org.springframework.ai.bedrock.converse.BedrockChatOptions
import org.springframework.ai.chat.prompt.ChatOptions

interface AiClientPort {

    fun <T> fetchEntity(promptText: String, modelRequest: AiModelRequest, responseType: Class<T>): T?

}
