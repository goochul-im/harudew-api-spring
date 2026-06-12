package b1a4.harudew.diary.adapter.`in`.web

import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.diary.adapter.dto.response.DiaryMapResponse
import b1a4.harudew.diary.application.port.`in`.DiaryQueryUseCase
import b1a4.harudew.member.domain.Member
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/map")
class MapController(
    private val diaryQueryUseCase: DiaryQueryUseCase
) {

    @GetMapping
    fun findMap(@CurrentMember member: Member): DiaryMapResponse =
        diaryQueryUseCase.findMap(member)
}
