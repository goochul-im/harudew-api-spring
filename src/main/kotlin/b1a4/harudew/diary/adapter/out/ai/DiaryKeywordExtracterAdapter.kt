package b1a4.harudew.diary.adapter.out.ai

import b1a4.harudew.diary.adapter.exception.DiaryAnalysisFailedException
import b1a4.harudew.diary.application.port.out.extract.DiaryKeywordExtracterPort
import b1a4.harudew.diary.application.port.out.extract.KeywordExtractResponse
import b1a4.harudew.global.infrastructure.ai.AiClientPort
import b1a4.harudew.global.infrastructure.ai.AiModelRequest
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

@Component
class DiaryKeywordExtracterAdapter(
    private val aiClient: AiClientPort,
    @Value("classpath:/prompts/keyword-extract.st")
    private val prompt: Resource
) : DiaryKeywordExtracterPort {

    override fun extract(content: String): KeywordExtractResponse? {
        val template = PromptTemplate(prompt)
        val renderedPrompt = template.render(mapOf("content" to content))

        val request = AiModelRequest(
            modelId = "apac.anthropic.claude-sonnet-4-20250514-v1:0",
            temperature = 0.05,
            topP = 0.9,
            topK = 10,
            maxTokens = 4000
        )

        try {
            return aiClient.fetchEntity(renderedPrompt, request, KeywordExtractResponse::class.java)
        } catch (e: Exception) {
            throw DiaryAnalysisFailedException(
                e,
                mapOf(
                    "content" to content
                )
            )
        }
    }

}
