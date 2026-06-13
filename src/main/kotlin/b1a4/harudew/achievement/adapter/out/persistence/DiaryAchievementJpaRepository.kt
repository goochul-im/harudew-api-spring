package b1a4.harudew.achievement.adapter.out.persistence

import b1a4.harudew.achievement.adapter.out.persistence.entity.DiaryAchievementEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DiaryAchievementJpaRepository : JpaRepository<DiaryAchievementEntity, Long> {
    fun findByDiaryIdIn(diaryIds: List<Long>): List<DiaryAchievementEntity>
}
