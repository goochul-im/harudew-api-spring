package b1a4.harudew.emotion.application.port.out

import b1a4.harudew.emotion.domain.DiaryActivityEmotion

interface DiaryActivityEmotionRepository {

    fun saveAll(diaryActivityEmotions: List<DiaryActivityEmotion>): List<DiaryActivityEmotion>
}
