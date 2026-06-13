package b1a4.harudew.achievement.adapter.out.persistence

import b1a4.harudew.achievement.adapter.out.persistence.entity.DiaryAchievementEntity
import b1a4.harudew.achievement.application.port.out.DiaryAchievementRepository
import b1a4.harudew.achievement.domain.DiaryAchievement
import org.springframework.stereotype.Repository

@Repository
class DiaryAchievementRepositoryImpl(
    private val diaryAchievementJpaRepository: DiaryAchievementJpaRepository
) : DiaryAchievementRepository {

    override fun saveAll(diaryAchievements: List<DiaryAchievement>): List<DiaryAchievement> {
        val entities = diaryAchievements.map { DiaryAchievementEntity.fromDomain(it) }
        return diaryAchievementJpaRepository.saveAll(entities).map { it.toDomain() }
    }

    override fun findByDiaryIds(diaryIds: List<Long>): List<DiaryAchievement> {
        if (diaryIds.isEmpty()) return emptyList()
        return diaryAchievementJpaRepository.findByDiaryIdIn(diaryIds).map { it.toDomain() }
    }
}
