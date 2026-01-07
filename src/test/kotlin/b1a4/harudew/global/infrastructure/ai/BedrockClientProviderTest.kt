package b1a4.harudew.global.infrastructure.ai

import b1a4.harudew.global.infrastructure.ai.dto.AiModelRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
class BedrockClientProviderTest {

    @Autowired
    private lateinit var bedrockClientProvider: BedrockClientProvider

    @Test
    fun `Bedrock과 상호작용하여 응답을 받아온다`() {
        // given
        // TODO: 사용자가 테스트하려는 실제 모델 ID와 파라미터로 아래 값을 변경해야 합니다.
        // AWS Bedrock에서 사용 가능한 모델 ID 예시:
        // - Claude 4 Sonnet: "apac.anthropic.claude-sonnet-4-20250514-v1:0"
        // - Claude 3.5 Sonnet: "anthropic.claude-3-5-sonnet-20240620-v1:0"
        // - Claude 3 Haiku: "anthropic.claude-3-haiku-20240307-v1:0"
        // - Titan Text G1 - Lite: "amazon.titan-text-lite-v1"
        val modelId = "apac.anthropic.claude-sonnet-4-20250514-v1:0"
        val temperature = 0.5
        val maxTokens = 500

        val request = AiModelRequest(
            modelId = modelId,
            temperature = temperature,
            maxTokens = maxTokens
        )

        // JSON 형식의 응답을 유도하는 프롬프트
        val promptText = """
            다음 내용을 JSON 형식으로 응답해주세요.
            {
                "greeting": "Hello, Bedrock!",
                "status": "success"
            }
            추가적인 설명 없이 오직 JSON만 출력하세요.
        """.trimIndent()

        // when
        // Map으로 결과를 받아 JSON 파싱 및 통신 성공 여부를 확인합니다.
        @Suppress("UNCHECKED_CAST")
        val result = bedrockClientProvider.fetchEntity(promptText, request, Map::class.java) as? Map<String, Any>

        // then
        println("Bedrock 응답 결과: $result")
        
        assertThat(result).isNotNull
        assertThat(result).containsKey("greeting")
        assertThat(result!!["greeting"]).isEqualTo("Hello, Bedrock!")
    }
}
