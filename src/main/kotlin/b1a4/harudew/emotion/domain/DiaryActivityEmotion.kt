package b1a4.harudew.emotion.domain

data class DiaryActivityEmotion(
    val id: Long? = null,
    val diaryId: Long,
    val activityName: String, // TODO: Activity 엔티티 구현 후 FK로 전환
    val emotion: EmotionType,
    val emotionBase: EmotionBase,
    val intensity: Int
)
