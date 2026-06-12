package b1a4.harudew.emotion.adapter.`in`.web

import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.emotion.adapter.dto.response.EmotionAnalysisPeriodResponse
import b1a4.harudew.emotion.adapter.dto.response.NegativeEmotionByActivityResponse
import b1a4.harudew.emotion.adapter.dto.response.NegativeEmotionByTargetResponse
import b1a4.harudew.emotion.adapter.dto.response.PositiveEmotionByActivityResponse
import b1a4.harudew.emotion.adapter.dto.response.PositiveEmotionByTargetResponse
import b1a4.harudew.emotion.domain.EmotionGroup
import b1a4.harudew.member.application.service.AnalysisAggregationService
import b1a4.harudew.member.domain.Member
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/emotion")
class EmotionController(
    private val analysisAggregationService: AnalysisAggregationService
) {

    @GetMapping
    fun getAll(
        @CurrentMember member: Member,
        @RequestParam emotion: EmotionGroup,
        @RequestParam(required = false, defaultValue = "7") period: Int
    ): EmotionAnalysisPeriodResponse =
        EmotionAnalysisPeriodResponse(
            activities = analysisAggregationService.activitySummary(member.id, emotion, period),
            people = analysisAggregationService.targetSummary(member.id, emotion, period),
            date = analysisAggregationService.dateSummary(member.id, emotion, period)
        )

    @GetMapping("/negative")
    fun negative(
        @CurrentMember member: Member,
        @RequestParam(required = false, defaultValue = "7") period: Int
    ): NegativeEmotionByTargetResponse =
        NegativeEmotionByTargetResponse(
            stressTarget = analysisAggregationService.targetSummary(member.id, EmotionGroup.스트레스, period),
            depressionTarget = analysisAggregationService.targetSummary(member.id, EmotionGroup.우울, period),
            anxietyTarget = analysisAggregationService.targetSummary(member.id, EmotionGroup.불안, period),
            stressDate = analysisAggregationService.dateSummary(member.id, EmotionGroup.스트레스, period),
            depressionDate = analysisAggregationService.dateSummary(member.id, EmotionGroup.우울, period),
            anxietyDate = analysisAggregationService.dateSummary(member.id, EmotionGroup.불안, period)
        )

    @GetMapping("/positive")
    fun positive(
        @CurrentMember member: Member,
        @RequestParam(required = false, defaultValue = "7") period: Int
    ): PositiveEmotionByTargetResponse =
        PositiveEmotionByTargetResponse(
            stabilityTarget = analysisAggregationService.targetSummary(member.id, EmotionGroup.안정, period),
            bondTarget = analysisAggregationService.targetSummary(member.id, EmotionGroup.유대, period),
            vitalityTarget = analysisAggregationService.targetSummary(member.id, EmotionGroup.활력, period),
            stabilityDate = analysisAggregationService.dateSummary(member.id, EmotionGroup.안정, period),
            bondDate = analysisAggregationService.dateSummary(member.id, EmotionGroup.유대, period),
            vitalityDate = analysisAggregationService.dateSummary(member.id, EmotionGroup.활력, period)
        )

    @GetMapping("/activity/negative")
    fun negativeActivities(
        @CurrentMember member: Member,
        @RequestParam(required = false, defaultValue = "7") period: Int
    ): NegativeEmotionByActivityResponse =
        NegativeEmotionByActivityResponse(
            stress = analysisAggregationService.activitySummary(member.id, EmotionGroup.스트레스, period),
            depression = analysisAggregationService.activitySummary(member.id, EmotionGroup.우울, period),
            anxiety = analysisAggregationService.activitySummary(member.id, EmotionGroup.불안, period)
        )

    @GetMapping("/activity/positive")
    fun positiveActivities(
        @CurrentMember member: Member,
        @RequestParam(required = false, defaultValue = "7") period: Int
    ): PositiveEmotionByActivityResponse =
        PositiveEmotionByActivityResponse(
            stability = analysisAggregationService.activitySummary(member.id, EmotionGroup.안정, period),
            bond = analysisAggregationService.activitySummary(member.id, EmotionGroup.유대, period),
            vitality = analysisAggregationService.activitySummary(member.id, EmotionGroup.활력, period)
        )

    @GetMapping("/empty-check")
    fun emptyCheck(@CurrentMember member: Member): Map<String, Boolean> =
        mapOf("isEmpty" to analysisAggregationService.memberSummary(member.id, 365).emotionPerDate.isEmpty())

    @GetMapping("/target/{targetId}")
    fun target(
        @CurrentMember member: Member,
        @PathVariable targetId: Long
    ) = analysisAggregationService.relationDetail(member.id, targetId).emotions
}
