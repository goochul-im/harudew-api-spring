package b1a4.harudew.recommend.application.service

import b1a4.harudew.diary.application.port.out.DiaryRepository
import b1a4.harudew.emotion.domain.EmotionType
import b1a4.harudew.recommend.adapter.dto.response.RecommendVideoResponse
import org.springframework.stereotype.Service

@Service
class RecommendService(
    private val diaryRepository: DiaryRepository
) {

    fun video(memberId: String, period: Int): RecommendVideoResponse {
        val diaries = diaryRepository.findByAuthorId(memberId, cursor = null, limit = period.coerceIn(1, 100))
        val emotion = diaries.asSequence()
            .flatMap { emotionNamesFromMetadata(it.metaData).asSequence() }
            .firstOrNull()
            ?: "평온"
        return RecommendVideoResponse(
            videoId = fallbackVideoIds(emotion),
            emotion = emotion,
            message = "${period}일 내의 ${emotion} 추천 영상입니다"
        )
    }

    fun activityComment(): Map<String, Any?> =
        mapOf<String, Any?>(
            "comment" to "오늘은 무리하지 않고 몸을 가볍게 움직여보세요.",
            "diaryId" to null
        )

    fun youtubeByEmotion(emotion: String): Map<String, List<String>> =
        mapOf("videoId" to fallbackVideoIds(emotion))

    private fun fallbackVideoIds(emotion: String): List<String> =
        when (EmotionType.entries.firstOrNull { it.name == emotion }) {
            EmotionType.불안, EmotionType.긴장, EmotionType.초조 -> listOf("inpok4MKVLM")
            EmotionType.우울, EmotionType.슬픔, EmotionType.무기력 -> listOf("ZToicYcHIOU")
            EmotionType.행복, EmotionType.기쁨, EmotionType.활력 -> listOf("2OEL4P1Rz04")
            else -> listOf("inpok4MKVLM")
        }

    private fun emotionNamesFromMetadata(metaData: Any): List<String> {
        val root = metaData as? Map<*, *> ?: return emptyList()
        val activities = root["activity_analysis"] as? List<*> ?: root["activityAnalysis"] as? List<*> ?: return emptyList()
        return activities.flatMap { activity ->
            val map = activity as? Map<*, *> ?: return@flatMap emptyList()
            listOf("self_emotions", "state_emotions", "selfEmotions", "stateEmotions")
                .flatMap { key -> extractEmotionNames(map[key]) }
        }
    }

    private fun extractEmotionNames(raw: Any?): List<String> {
        if (raw is Map<*, *>) return (raw["emotion"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        if (raw is List<*>) return raw.mapNotNull { item -> (item as? Map<*, *>)?.get("emotion")?.toString() }
        return emptyList()
    }
}
