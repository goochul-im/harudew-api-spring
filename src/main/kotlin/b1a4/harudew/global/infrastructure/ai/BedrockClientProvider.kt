package b1a4.harudew.global.infrastructure.ai

import b1a4.harudew.global.infrastructure.ai.dto.AiModelRequest
import org.springframework.ai.bedrock.converse.BedrockChatOptions
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Component

@Component
class BedrockClientProvider(
    private val chatClient: ChatClient
) : AiClientPort {

    override fun <T> fetchEntity(promptText: String, modelRequest: AiModelRequest, responseType: Class<T>): T? {
        val options = BedrockChatOptions.builder()
            .model(modelRequest.modelId)
            .temperature(modelRequest.temperature)
            .topP(modelRequest.topP)
            .topK(modelRequest.topK)
            .maxTokens(modelRequest.maxTokens)
            .build()
        val prompt = Prompt(promptText, options)

        return chatClient.prompt(prompt)
            .call()
            .entity(responseType)
    }

}
