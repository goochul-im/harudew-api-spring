package b1a4.harudew.diary.adapter.out.ai

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@Tag("integration")
class DiaryTaggingAdapterIntegrationTest {

    @Autowired
    lateinit var diaryTaggingAdapter: DiaryTaggingAdapter

    @Test
    fun `실제 AI 모델을 호출하여 일기 분석 결과를 반환한다`() {
        // given
        val content = """
            오늘 하루는 정말 바빴다. 아침 일찍 일어나서 운동을 갔는데, 생각보다 몸이 무거워서 힘들었다.
            그래도 운동을 마치고 나니 상쾌했다. 회사에서는 영희와 작은 언쟁이 있었지만, 잘 해결되어서 다행이다.
            점심에는 맛있는 파스타를 먹었고, 저녁에는 친구를 만나 수다를 떨었다.
            전반적으로 피곤하지만 보람찬 하루였다.
        """.trimIndent()

        // when
        println("AI 분석 요청 중... (시간이 소요될 수 있습니다)")
        val result = diaryTaggingAdapter.tag(content)

        // then
        assertThat(result).isNotNull
        assertThat(result?.result).isNotEmpty

        println("AI 분석 결과:")
        println(result)
    }

}
