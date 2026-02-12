package b1a4.harudew.global.orchestration

import b1a4.harudew.diary.application.port.`in`.DiaryRagPreprocessingCommand
import b1a4.harudew.diary.application.port.`in`.DiaryPreprocessingUseCase
import b1a4.harudew.diary.application.port.out.DiaryProblemRepository
import b1a4.harudew.diary.application.port.out.DiaryReflectionRepository
import b1a4.harudew.diary.application.port.out.analysis.DiaryAnalysisResponse
import b1a4.harudew.diary.domain.event.DiaryCreateEvent
import b1a4.harudew.diary.domain.model.DiaryProblem
import b1a4.harudew.diary.domain.model.DiaryReflection
import b1a4.harudew.emotion.application.port.out.DiaryActivityEmotionRepository
import b1a4.harudew.emotion.domain.DiaryActivityEmotion
import b1a4.harudew.emotion.domain.EmotionBase
import b1a4.harudew.emotion.domain.EmotionType
import b1a4.harudew.person.application.port.out.DiaryPersonEmotionRepository
import b1a4.harudew.person.domain.DiaryPersonEmotion
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Diary에 관련된 부가 관심사를 이벤트 기반으로 분리해둔 클래스입니다
 * 각 이벤트에 맞게 적절한 도메인을 호출합니다
 */
@Component
class DiaryEventHandler(
    private val diaryPreprocessingUseCase: DiaryPreprocessingUseCase,
    private val diaryReflectionRepository: DiaryReflectionRepository,
    private val diaryProblemRepository: DiaryProblemRepository,
    private val diaryActivityEmotionRepository: DiaryActivityEmotionRepository,
    private val diaryPersonEmotionRepository: DiaryPersonEmotionRepository
) {

    private val logger = LoggerFactory.getLogger(DiaryEventHandler::class.java)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun createHandler(event: DiaryCreateEvent) {
        diaryPreprocessingUseCase.ragPreprocessing(
            DiaryRagPreprocessingCommand(
                content = event.content,
                diaryId = event.diaryId,
                authorId = event.authorId,
                writtenDate = event.writtenDate
            )
        )

        saveAnalysisToEntities(event.diaryId, event.analysisResult)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveAnalysisToEntities(diaryId: Long, analysisResult: DiaryAnalysisResponse) {
        try {
            saveActivityEmotions(diaryId, analysisResult)
            saveProblems(diaryId, analysisResult)
            savePersonEmotions(diaryId, analysisResult)
            saveReflection(diaryId, analysisResult)
        } catch (e: Exception) {
            logger.error("분석 결과 정규화 저장 실패 (diaryId={}). metaData에 원본 보존됨.", diaryId, e)
        }
    }

    private fun saveActivityEmotions(diaryId: Long, analysisResult: DiaryAnalysisResponse) {
        val activityEmotions = analysisResult.activityAnalysis.flatMap { activity ->
            val selfEmotions = activity.selfEmotions.mapNotNull { emotionData ->
                parseEmotionType(emotionData.emotion)?.let { emotionType ->
                    DiaryActivityEmotion(
                        diaryId = diaryId,
                        activityName = activity.activity,
                        emotion = emotionType,
                        emotionBase = EmotionBase.SELF,
                        intensity = emotionData.emotionIntensity
                    )
                }
            }
            val stateEmotions = activity.stateEmotions.mapNotNull { emotionData ->
                parseEmotionType(emotionData.emotion)?.let { emotionType ->
                    DiaryActivityEmotion(
                        diaryId = diaryId,
                        activityName = activity.activity,
                        emotion = emotionType,
                        emotionBase = EmotionBase.STATE,
                        intensity = emotionData.emotionIntensity
                    )
                }
            }
            selfEmotions + stateEmotions
        }

        if (activityEmotions.isNotEmpty()) {
            diaryActivityEmotionRepository.saveAll(activityEmotions)
        }
    }

    private fun saveProblems(diaryId: Long, analysisResult: DiaryAnalysisResponse) {
        val problems = analysisResult.activityAnalysis.flatMap { activity ->
            activity.problem.map { problem ->
                DiaryProblem(
                    diaryId = diaryId,
                    activityName = activity.activity,
                    situation = problem.situation,
                    approach = problem.approach,
                    outcome = problem.outcome,
                    conflictResponseCode = problem.conflictResponseCode
                )
            }
        }

        if (problems.isNotEmpty()) {
            diaryProblemRepository.saveAll(problems)
        }
    }

    private fun savePersonEmotions(diaryId: Long, analysisResult: DiaryAnalysisResponse) {
        val personEmotions = analysisResult.activityAnalysis.flatMap { activity ->
            activity.peoples.flatMap { person ->
                val intimacy = person.nameIntimacy.toFloatOrNull() ?: 0.5f
                person.interactions.mapNotNull { emotionData ->
                    parseEmotionType(emotionData.emotion)?.let { emotionType ->
                        DiaryPersonEmotion(
                            diaryId = diaryId,
                            personName = person.name,
                            emotion = emotionType,
                            intensity = emotionData.emotionIntensity,
                            nameIntimacy = intimacy
                        )
                    }
                }
            }
        }

        if (personEmotions.isNotEmpty()) {
            diaryPersonEmotionRepository.saveAll(personEmotions)
        }
    }

    private fun saveReflection(diaryId: Long, analysisResult: DiaryAnalysisResponse) {
        val reflection = analysisResult.reflection
        diaryReflectionRepository.save(
            DiaryReflection(
                diaryId = diaryId,
                shortcomings = reflection.shortcomings,
                tomorrowMindset = reflection.tomorrowMindSet
            )
        )
    }

    private fun parseEmotionType(emotion: String): EmotionType? {
        return try {
            EmotionType.valueOf(emotion)
        } catch (e: IllegalArgumentException) {
            logger.warn("알 수 없는 감정 타입: {}", emotion)
            null
        }
    }
}
