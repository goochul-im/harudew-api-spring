package b1a4.harudew.emotion.domain

import b1a4.harudew.diary.domain.model.Diary

class DiaryEmotion(
    val id: Long,
    val diary: Diary,
    val emotion: EmotionType,
    val emotionBase: EmotionBase,
    val emotionGroup: EmotionGroup,
    val intensity: Float
) {
}
