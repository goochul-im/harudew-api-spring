package b1a4.harudew.person.adapter.out.persistence

import b1a4.harudew.person.adapter.out.persistence.entity.DiaryPersonEmotionEntity
import b1a4.harudew.person.application.port.out.DiaryPersonEmotionRepository
import b1a4.harudew.person.domain.DiaryPersonEmotion
import org.springframework.stereotype.Repository

@Repository
class DiaryPersonEmotionRepositoryImpl(
    private val diaryPersonEmotionJpaRepository: DiaryPersonEmotionJpaRepository
) : DiaryPersonEmotionRepository {

    override fun saveAll(diaryPersonEmotions: List<DiaryPersonEmotion>): List<DiaryPersonEmotion> {
        val entities = diaryPersonEmotions.map { DiaryPersonEmotionEntity.fromDomain(it) }
        return diaryPersonEmotionJpaRepository.saveAll(entities).map { it.toDomain() }
    }
}
