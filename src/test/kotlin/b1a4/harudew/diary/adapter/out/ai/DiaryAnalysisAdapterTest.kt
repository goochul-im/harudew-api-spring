package b1a4.harudew.diary.adapter.out.ai

import b1a4.harudew.diary.application.port.out.dto.DiaryAnalysisResponse
import b1a4.harudew.diary.application.port.out.dto.Reflection
import b1a4.harudew.global.infrastructure.ai.AiClientPort
import b1a4.harudew.global.infrastructure.ai.dto.AiModelRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource

@ExtendWith(MockitoExtension::class)
class DiaryAnalysisAdapterTest {

    @Mock
    lateinit var aiClient: AiClientPort

    @Test
    fun `일기 분석 요청 시 AI 클라이언트를 호출하고 결과를 반환한다`() {
        // given
        val promptContent = "Analyze this: {content}"
        val promptResource: Resource = ByteArrayResource(promptContent.toByteArray())
        val adapter = DiaryAnalysisAdapter(aiClient, promptResource)

        val content = "Today was a good day."
        val expectedResponse = DiaryAnalysisResponse(
            activityAnalysis = emptyList(),
            reflection = Reflection(
                achievements = listOf("Goal reached"),
                shortcomings = emptyList(),
                todo = listOf("Sleep")
            )
        )
        val expectedRenderedPrompt = "Analyze this: Today was a good day."
        val expectedRequest = AiModelRequest(
            modelId = "apac.anthropic.claude-sonnet-4-20250514-v1:0",
            temperature = 0.05,
            topP = 0.9,
            topK = 10,
            maxTokens = 4000
        )

        `when`(aiClient.fetchEntity(
            safeEq(expectedRenderedPrompt),
            safeEq(expectedRequest),
            safeEq(DiaryAnalysisResponse::class.java)
        )).thenReturn(expectedResponse)

        // when
        val result = adapter.getAnalysis(content)

        // then
        assertThat(result).isNotNull
        assertThat(result).isEqualTo(expectedResponse)

        verify(aiClient).fetchEntity(
            safeEq(expectedRenderedPrompt),
            safeEq(expectedRequest),
            safeEq(DiaryAnalysisResponse::class.java)
        )
    }

    private fun <T> safeEq(value: T): T {
        eq(value)
        return value
    }
}
