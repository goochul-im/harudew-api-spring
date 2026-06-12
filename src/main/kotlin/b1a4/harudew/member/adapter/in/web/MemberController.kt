package b1a4.harudew.member.adapter.`in`.web

import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.member.adapter.dto.response.CharacterRes
import b1a4.harudew.member.adapter.dto.response.EmotionBaseAnalysisRes
import b1a4.harudew.member.adapter.dto.response.MemberSummaryRes
import b1a4.harudew.member.application.service.AnalysisAggregationService
import b1a4.harudew.member.domain.Member
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/member")
class MemberController(
    private val analysisAggregationService: AnalysisAggregationService
) {

    @GetMapping("/summary")
    fun summary(
        @CurrentMember member: Member,
        @RequestParam(required = false) period: Int?,
        @RequestParam(required = false) days: Int?
    ): MemberSummaryRes =
        analysisAggregationService.memberSummary(member.id, period ?: days ?: 7)

    @GetMapping("/emotion/base-analysis")
    fun baseAnalysis(@CurrentMember member: Member): EmotionBaseAnalysisRes =
        analysisAggregationService.emotionBaseAnalysis(member.id)

    @GetMapping("/emotion/base-analysis/date")
    fun baseAnalysisByDate(
        @CurrentMember member: Member,
        @RequestParam year: Int,
        @RequestParam month: Int
    ): EmotionBaseAnalysisRes =
        analysisAggregationService.emotionBaseAnalysis(member.id, year, month)

    @GetMapping("/character")
    fun character(@CurrentMember member: Member): CharacterRes =
        CharacterRes(analysisAggregationService.character(member.id))

    @PostMapping("/test/stress")
    fun stressTest(): Map<String, String> =
        mapOf("message" to "스트레스 테스트 날짜가 갱신되었습니다.")

    @PostMapping("/test/anxiety")
    fun anxietyTest(): Map<String, String> =
        mapOf("message" to "불안 테스트 날짜가 갱신되었습니다.")

    @PostMapping("/test/depression")
    fun depressionTest(): Map<String, String> =
        mapOf("message" to "우울 테스트 날짜가 갱신되었습니다.")
}
