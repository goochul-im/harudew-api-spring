package b1a4.harudew.global.orchestration

import b1a4.harudew.achievement.application.port.out.DiaryAchievementRepository
import b1a4.harudew.achievement.domain.DiaryAchievement
import b1a4.harudew.diary.application.port.`in`.DiaryPreprocessingUseCase
import b1a4.harudew.diary.application.port.out.DiaryProblemRepository
import b1a4.harudew.diary.application.port.out.DiaryReflectionRepository
import b1a4.harudew.diary.application.port.out.analysis.ActivityAnalysis
import b1a4.harudew.diary.application.port.out.analysis.DiaryAnalysisResponse
import b1a4.harudew.diary.application.port.out.analysis.EmotionData
import b1a4.harudew.diary.application.port.out.analysis.PersonAnalysis
import b1a4.harudew.diary.application.port.out.analysis.ProblemAnalysis
import b1a4.harudew.diary.application.port.out.analysis.Reflection
import b1a4.harudew.diary.domain.model.DiaryProblem
import b1a4.harudew.diary.domain.model.DiaryReflection
import b1a4.harudew.emotion.application.port.out.DiaryActivityEmotionRepository
import b1a4.harudew.emotion.domain.DiaryActivityEmotion
import b1a4.harudew.emotion.domain.EmotionBase
import b1a4.harudew.emotion.domain.EmotionType
import b1a4.harudew.person.application.port.out.DiaryPersonEmotionRepository
import b1a4.harudew.person.domain.DiaryPersonEmotion
import b1a4.harudew.todo.application.port.out.DiaryTodoRepository
import b1a4.harudew.todo.domain.DiaryTodo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class DiaryEventHandlerTest {

    @Mock lateinit var diaryPreprocessingUseCase: DiaryPreprocessingUseCase
    @Mock lateinit var diaryReflectionRepository: DiaryReflectionRepository
    @Mock lateinit var diaryProblemRepository: DiaryProblemRepository
    @Mock lateinit var diaryActivityEmotionRepository: DiaryActivityEmotionRepository
    @Mock lateinit var diaryPersonEmotionRepository: DiaryPersonEmotionRepository
    @Mock lateinit var diaryAchievementRepository: DiaryAchievementRepository
    @Mock lateinit var diaryTodoRepository: DiaryTodoRepository

    lateinit var handler: DiaryEventHandler

    @BeforeEach
    fun setUp() {
        handler = DiaryEventHandler(
            diaryPreprocessingUseCase = diaryPreprocessingUseCase,
            diaryReflectionRepository = diaryReflectionRepository,
            diaryProblemRepository = diaryProblemRepository,
            diaryActivityEmotionRepository = diaryActivityEmotionRepository,
            diaryPersonEmotionRepository = diaryPersonEmotionRepository,
            diaryAchievementRepository = diaryAchievementRepository,
            diaryTodoRepository = diaryTodoRepository
        )
    }

    @Test
    @DisplayName("saveAnalysisToEntities: 분석 결과 전체를 정규화 저장 포트로 분배한다")
    fun `saveAnalysisToEntities saves normalized analysis result`() {
        // given
        val analysis = DiaryAnalysisResponse(
            activityAnalysis = listOf(
                ActivityAnalysis(
                    activity = "산책",
                    peoples = listOf(
                        PersonAnalysis(
                            name = "민수",
                            interactions = listOf(EmotionData("감사", 3)),
                            nameIntimacy = "0.8"
                        )
                    ),
                    selfEmotions = listOf(EmotionData("뿌듯함", 4)),
                    stateEmotions = listOf(EmotionData("평온", 2)),
                    problem = listOf(
                        ProblemAnalysis(
                            situation = "비가 올까 걱정했다",
                            approach = "우산을 챙겼다",
                            outcome = "편하게 걸었다",
                            conflictResponseCode = "PLAN_AHEAD"
                        )
                    ),
                    strength = "준비성"
                )
            ),
            reflection = Reflection(
                achievements = listOf("산책을 끝까지 했다"),
                shortcomings = listOf("출발이 늦었다"),
                todo = listOf("내일도 걷기"),
                tomorrowMindSet = "가볍게 시작하기"
            )
        )

        // when
        handler.saveAnalysisToEntities(
            diaryId = 10L,
            authorId = "member-1",
            writtenDate = LocalDate.of(2026, 6, 12),
            analysisResult = analysis
        )

        // then
        val activityEmotionCaptor = argumentCaptor<List<DiaryActivityEmotion>>()
        verify(diaryActivityEmotionRepository).saveAll(activityEmotionCaptor.capture())
        assertThat(activityEmotionCaptor.firstValue)
            .extracting("diaryId", "activityName", "emotion", "emotionBase", "intensity")
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(10L, "산책", EmotionType.뿌듯함, EmotionBase.SELF, 4),
                org.assertj.core.groups.Tuple.tuple(10L, "산책", EmotionType.평온, EmotionBase.STATE, 2)
            )

        val problemCaptor = argumentCaptor<List<DiaryProblem>>()
        verify(diaryProblemRepository).saveAll(problemCaptor.capture())
        assertThat(problemCaptor.firstValue.single()).isEqualTo(
            DiaryProblem(
                diaryId = 10L,
                activityName = "산책",
                situation = "비가 올까 걱정했다",
                approach = "우산을 챙겼다",
                outcome = "편하게 걸었다",
                conflictResponseCode = "PLAN_AHEAD"
            )
        )

        val personEmotionCaptor = argumentCaptor<List<DiaryPersonEmotion>>()
        verify(diaryPersonEmotionRepository).saveAll(personEmotionCaptor.capture())
        assertThat(personEmotionCaptor.firstValue.single()).isEqualTo(
            DiaryPersonEmotion(
                diaryId = 10L,
                personName = "민수",
                emotion = EmotionType.감사,
                intensity = 3,
                nameIntimacy = 0.8f
            )
        )

        val reflectionCaptor = argumentCaptor<DiaryReflection>()
        verify(diaryReflectionRepository).save(reflectionCaptor.capture())
        assertThat(reflectionCaptor.firstValue).isEqualTo(
            DiaryReflection(
                diaryId = 10L,
                shortcomings = listOf("출발이 늦었다"),
                tomorrowMindset = "가볍게 시작하기"
            )
        )

        val achievementCaptor = argumentCaptor<List<DiaryAchievement>>()
        verify(diaryAchievementRepository).saveAll(achievementCaptor.capture())
        assertThat(achievementCaptor.firstValue.single()).isEqualTo(
            DiaryAchievement(
                diaryId = 10L,
                content = "산책을 끝까지 했다"
            )
        )

        val todoCaptor = argumentCaptor<List<DiaryTodo>>()
        verify(diaryTodoRepository).saveAll(todoCaptor.capture())
        assertThat(todoCaptor.firstValue.single()).isEqualTo(
            DiaryTodo(
                diaryId = 10L,
                authorId = "member-1",
                content = "내일도 걷기",
                createdAt = LocalDate.of(2026, 6, 12)
            )
        )
    }
}
