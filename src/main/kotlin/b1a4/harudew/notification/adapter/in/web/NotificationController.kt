package b1a4.harudew.notification.adapter.`in`.web

import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.member.domain.Member
import b1a4.harudew.notification.adapter.dto.response.NotificationResponse
import b1a4.harudew.notification.adapter.dto.response.UnreadNotificationCountResponse
import b1a4.harudew.notification.application.service.NotificationService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/noti")
class NotificationController(
    private val notificationService: NotificationService
) {

    @GetMapping
    fun unread(@CurrentMember member: Member): List<NotificationResponse> =
        notificationService.findUnread(member.id)

    @GetMapping("/all")
    fun all(@CurrentMember member: Member): List<NotificationResponse> =
        notificationService.findAll(member.id)

    @GetMapping("/count")
    fun count(@CurrentMember member: Member): UnreadNotificationCountResponse =
        notificationService.countUnread(member.id)

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun read(
        @CurrentMember member: Member,
        @PathVariable id: Long
    ) {
        notificationService.read(member.id, id)
    }

    @PostMapping("/test-create")
    fun testCreate(@CurrentMember member: Member): NotificationResponse =
        notificationService.createTest(member.id)
}
