package b1a4.harudew.person.domain

import b1a4.harudew.emotion.domain.EmotionType

data class DiaryPersonEmotion(
    val id: Long? = null,
    val diaryId: Long,
    val personName: String, // TODO: Person 엔티티 구현 후 FK로 전환
    val emotion: EmotionType,
    val intensity: Int,
    val nameIntimacy: Float
)
