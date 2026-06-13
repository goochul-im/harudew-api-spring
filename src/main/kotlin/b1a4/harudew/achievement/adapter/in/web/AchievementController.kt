package b1a4.harudew.achievement.adapter.`in`.web

import b1a4.harudew.achievement.adapter.dto.response.AchievementResponse
import b1a4.harudew.achievement.adapter.dto.response.AllAchievementResponse
import b1a4.harudew.achievement.application.port.out.DiaryAchievementRepository
import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.diary.application.port.out.DiaryRepository
import b1a4.harudew.member.domain.Member
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/achievement")
class AchievementController(
    private val diaryRepository: DiaryRepository,
    private val diaryAchievementRepository: DiaryAchievementRepository
) {

    @GetMapping("/all")
    fun all(@CurrentMember member: Member): AllAchievementResponse {
        val diaryIds = diaryRepository.findByAuthorId(member.id, cursor = null, limit = 1000)
            .mapNotNull { it.id }
        val achievements = diaryAchievementRepository.findByDiaryIds(diaryIds)
            .groupBy { it.content }
            .entries
            .mapIndexed { index, entry ->
                AchievementResponse(
                    id = entry.value.firstOrNull()?.clusterId ?: entry.value.firstOrNull()?.id ?: (index + 1L),
                    label = entry.key,
                    count = entry.value.size
                )
            }
            .sortedByDescending { it.count }

        return AllAchievementResponse(achievements)
    }
}
