package b1a4.harudew.recommend.adapter.`in`.web

import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.member.domain.Member
import b1a4.harudew.recommend.adapter.dto.response.RecommendVideoResponse
import b1a4.harudew.recommend.application.service.RecommendService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/recommend")
class RecommendController(
    private val recommendService: RecommendService
) {

    @GetMapping("/video")
    fun video(
        @CurrentMember member: Member,
        @RequestParam(required = false, defaultValue = "10") period: Int
    ): RecommendVideoResponse = recommendService.video(member.id, period)

    @GetMapping("/activity/weekday/today")
    fun todayActivity(): Map<String, Any?> = recommendService.activityComment()

    @GetMapping("/activity/weekday/tomorrow")
    fun tomorrowActivity(): Map<String, Any?> = recommendService.activityComment()

    @GetMapping("/activity/weekday/{date}")
    fun dateActivity(@PathVariable date: String): Map<String, Any?> = recommendService.activityComment()
}

@RestController
@RequestMapping("/youtube")
class YoutubeController(
    private val recommendService: RecommendService
) {

    @GetMapping("/video")
    fun video(@RequestParam emotion: String): Map<String, List<String>> =
        recommendService.youtubeByEmotion(emotion)

    @PostMapping("/getVideo")
    fun refresh(): Map<String, String> =
        mapOf("message" to "deterministic fallback videos are ready")
}
