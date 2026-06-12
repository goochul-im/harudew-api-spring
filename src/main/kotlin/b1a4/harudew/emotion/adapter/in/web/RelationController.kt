package b1a4.harudew.emotion.adapter.`in`.web

import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.emotion.adapter.dto.response.GraphResponse
import b1a4.harudew.emotion.adapter.dto.response.TargetDetailAnalysisResponse
import b1a4.harudew.member.application.service.AnalysisAggregationService
import b1a4.harudew.member.domain.Member
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/relation")
class RelationController(
    private val analysisAggregationService: AnalysisAggregationService
) {

    @GetMapping
    fun graph(@CurrentMember member: Member): GraphResponse =
        analysisAggregationService.relationGraph(member.id)

    @GetMapping("/detail/{id}")
    fun detail(
        @CurrentMember member: Member,
        @PathVariable id: Long
    ): TargetDetailAnalysisResponse =
        analysisAggregationService.relationDetail(member.id, id)
}
