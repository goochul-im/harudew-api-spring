package b1a4.harudew.diary.adapter.out.local

import b1a4.harudew.diary.application.port.out.analysis.ActivityAnalysis
import b1a4.harudew.diary.application.port.out.analysis.DiaryAnalysisPort
import b1a4.harudew.diary.application.port.out.analysis.DiaryAnalysisResponse
import b1a4.harudew.diary.application.port.out.analysis.EmotionData
import b1a4.harudew.diary.application.port.out.analysis.Reflection
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("local", "test")
class LocalDiaryAnalysisAdapter : DiaryAnalysisPort {

    override fun getAnalysis(content: String): DiaryAnalysisResponse =
        DiaryAnalysisResponse(
            activityAnalysis = listOf(
                ActivityAnalysis(
                    activity = "일기 작성",
                    peoples = emptyList(),
                    selfEmotions = listOf(EmotionData("만족감", 4)),
                    stateEmotions = listOf(EmotionData("평온", 4)),
                    problem = emptyList(),
                    strength = "정직함"
                )
            ),
            reflection = Reflection(
                achievements = listOf("통합 검증 완료"),
                shortcomings = emptyList(),
                todo = listOf("내일도 기록하기"),
                tomorrowMindSet = "차분하게 이어가기"
            )
        )
}
