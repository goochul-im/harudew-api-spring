package b1a4.harudew.member.adapter.`in`.web

import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.member.adapter.dto.response.StrengthResponse
import b1a4.harudew.member.application.service.AnalysisAggregationService
import b1a4.harudew.member.domain.Member
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/strength")
class StrengthController(
    private val analysisAggregationService: AnalysisAggregationService
) {

    @GetMapping
    fun strength(@CurrentMember member: Member): StrengthResponse =
        analysisAggregationService.strength(member.id)

    @GetMapping("/period")
    fun strengthByPeriod(
        @CurrentMember member: Member,
        @RequestParam year: Int,
        @RequestParam month: Int
    ): StrengthResponse =
        analysisAggregationService.strength(member.id, year, month)

    @GetMapping("/date")
    fun strengthByDate(
        @CurrentMember member: Member,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): StrengthResponse =
        analysisAggregationService.strength(member.id)
}
