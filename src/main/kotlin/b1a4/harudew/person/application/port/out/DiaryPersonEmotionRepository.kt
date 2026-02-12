package b1a4.harudew.person.application.port.out

import b1a4.harudew.person.domain.DiaryPersonEmotion

interface DiaryPersonEmotionRepository {

    fun saveAll(diaryPersonEmotions: List<DiaryPersonEmotion>): List<DiaryPersonEmotion>
}
