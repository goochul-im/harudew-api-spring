package b1a4.harudew.diary.adapter.out.ai

import b1a4.harudew.diary.application.port.out.DiaryAnalysisPort
import b1a4.harudew.diary.application.port.out.dto.DiaryAnalysisResponse
import b1a4.harudew.global.infrastructure.ai.AiClientPort
import b1a4.harudew.global.infrastructure.ai.dto.AiModelRequest
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

@Component
class DiaryAnalysisAdapter(
    private val aiClient: AiClientPort,
    @Value("classpath:/prompts/diary-analysis.st")
    private val prompt: Resource
) : DiaryAnalysisPort {

    override fun getAnalysis(content: String) : DiaryAnalysisResponse? {

        val template = PromptTemplate(prompt)
        val renderedPrompt = template.render(mapOf("content" to content))

        val request = AiModelRequest(
            modelId = "apac.anthropic.claude-sonnet-4-20250514-v1:0",
            temperature = 0.05,
            topP = 0.9,
            topK = 10,
            maxTokens = 4000
        )

        return aiClient.fetchEntity(renderedPrompt, request, DiaryAnalysisResponse::class.java)
    }

}
