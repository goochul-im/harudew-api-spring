package b1a4.harudew.recommend.adapter.`in`.web

import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.member.domain.Member
import b1a4.harudew.recommend.adapter.dto.response.RecommendRoutineResponse
import b1a4.harudew.recommend.adapter.dto.response.RoutineResponse
import b1a4.harudew.recommend.adapter.dto.response.RoutineToggleResponse
import b1a4.harudew.recommend.application.service.RoutineService
import b1a4.harudew.recommend.domain.RoutineType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.HttpStatus

@RestController
@RequestMapping("/routine")
class RoutineController(
    private val routineService: RoutineService
) {

    @GetMapping("/trigger")
    fun triggers(@CurrentMember member: Member): List<RoutineResponse> =
        routineService.findTriggers(member.id)

    @GetMapping("/{type}")
    fun byType(
        @CurrentMember member: Member,
        @PathVariable type: String
    ): List<RoutineResponse> = routineService.findByType(member.id, type.toRoutineType())

    @PostMapping("/{type}")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @CurrentMember member: Member,
        @PathVariable type: String,
        @RequestBody request: RoutineCreateRequest
    ): RoutineResponse = routineService.create(member.id, type.toRoutineType(), request.content)

    @GetMapping("/recommend")
    fun recommend(
        @CurrentMember member: Member,
        @RequestParam diaryId: Long
    ): RecommendRoutineResponse = routineService.recommend(member.id, diaryId)

    @PatchMapping("/{id}")
    fun toggle(
        @CurrentMember member: Member,
        @PathVariable id: Long
    ): RoutineToggleResponse = routineService.toggle(member.id, id)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @CurrentMember member: Member,
        @PathVariable id: Long
    ) {
        routineService.delete(member.id, id)
    }

    private fun String.toRoutineType(): RoutineType =
        RoutineType.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
            ?: throw IllegalArgumentException("지원하지 않는 routine type입니다: $this")
}

data class RoutineCreateRequest(
    val content: String
)
