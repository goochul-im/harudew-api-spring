package b1a4.harudew.emotion.adapter.out.persistence

import b1a4.harudew.emotion.adapter.out.persistence.entity.DiaryActivityEmotionEntity
import b1a4.harudew.emotion.application.port.out.DiaryActivityEmotionRepository
import b1a4.harudew.emotion.domain.DiaryActivityEmotion
import org.springframework.stereotype.Repository

@Repository
class DiaryActivityEmotionRepositoryImpl(
    private val diaryActivityEmotionJpaRepository: DiaryActivityEmotionJpaRepository
) : DiaryActivityEmotionRepository {

    override fun saveAll(diaryActivityEmotions: List<DiaryActivityEmotion>): List<DiaryActivityEmotion> {
        val entities = diaryActivityEmotions.map { DiaryActivityEmotionEntity.fromDomain(it) }
        return diaryActivityEmotionJpaRepository.saveAll(entities).map { it.toDomain() }
    }
}
