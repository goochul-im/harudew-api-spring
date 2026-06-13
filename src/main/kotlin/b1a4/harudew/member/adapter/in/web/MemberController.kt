package b1a4.harudew.member.adapter.`in`.web

import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.member.adapter.dto.response.CharacterRes
import b1a4.harudew.member.adapter.dto.response.EmotionBaseAnalysisRes
import b1a4.harudew.member.adapter.dto.response.EmotionSummaryWeekdayRes
import b1a4.harudew.member.adapter.dto.response.MemberSummaryRes
import b1a4.harudew.member.adapter.out.infrastructure.MemberEntity
import b1a4.harudew.member.adapter.out.infrastructure.MemberJpaRepository
import b1a4.harudew.member.application.service.AnalysisAggregationService
import b1a4.harudew.member.domain.Member
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/member")
class MemberController(
    private val analysisAggregationService: AnalysisAggregationService,
    private val memberJpaRepository: MemberJpaRepository
) {

    @GetMapping("/summary")
    fun summary(
        @CurrentMember member: Member,
        @RequestParam(required = false) period: Int?,
        @RequestParam(required = false) days: Int?
    ): MemberSummaryRes =
        analysisAggregationService.memberSummary(member.id, period ?: days ?: 7)

    @GetMapping("/emotion/weekday")
    fun weekdayEmotion(
        @CurrentMember member: Member,
        @RequestParam(required = false, defaultValue = "7") period: Int
    ): EmotionSummaryWeekdayRes =
        analysisAggregationService.weekdayEmotion(member.id, period)

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
    fun stressTest(@CurrentMember member: Member): Map<String, String> {
        updateTestDate(member, stress = LocalDate.now())
        return mapOf("message" to "스트레스 테스트 날짜가 갱신되었습니다.")
    }

    @PostMapping("/test/anxiety")
    fun anxietyTest(@CurrentMember member: Member): Map<String, String> {
        updateTestDate(member, anxiety = LocalDate.now())
        return mapOf("message" to "불안 테스트 날짜가 갱신되었습니다.")
    }

    @PostMapping("/test/depression")
    fun depressionTest(@CurrentMember member: Member): Map<String, String> {
        updateTestDate(member, depression = LocalDate.now())
        return mapOf("message" to "우울 테스트 날짜가 갱신되었습니다.")
    }

    private fun updateTestDate(
        member: Member,
        stress: LocalDate? = member.lastStressTestDate,
        anxiety: LocalDate? = member.lastAnxietyTestDate,
        depression: LocalDate? = member.lastDepressionTestDate
    ) {
        val entity = memberJpaRepository.findById(member.id)
            .orElseGet { MemberEntity.fromDomain(member) }
        entity.lastStressTestDate = stress
        entity.lastAnxietyTestDate = anxiety
        entity.lastDepressionTestDate = depression
        memberJpaRepository.save(entity)
    }
}
