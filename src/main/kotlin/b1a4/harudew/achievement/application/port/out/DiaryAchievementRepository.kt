package b1a4.harudew.achievement.application.port.out

import b1a4.harudew.achievement.domain.DiaryAchievement

interface DiaryAchievementRepository {

    fun saveAll(diaryAchievements: List<DiaryAchievement>): List<DiaryAchievement>
}
