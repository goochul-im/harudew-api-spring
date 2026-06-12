package b1a4.harudew.notification.adapter.`in`.web

import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.member.domain.Member
import b1a4.harudew.notification.application.service.TestNotificationRequest
import b1a4.harudew.notification.application.service.UnsubscribeRequest
import b1a4.harudew.notification.application.service.WebpushService
import b1a4.harudew.notification.application.service.WebpushSubscriptionRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/webpush")
class WebpushController(
    private val webpushService: WebpushService
) {

    @PostMapping("/subscribe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun subscribe(
        @CurrentMember member: Member,
        @RequestBody request: WebpushSubscriptionRequest
    ) {
        webpushService.subscribe(member.id, request)
    }

    @PostMapping("/unsubscribe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unsubscribe(
        @CurrentMember member: Member,
        @RequestBody request: UnsubscribeRequest
    ) {
        webpushService.unsubscribe(member.id, request.endpoint)
    }

    @GetMapping("/status")
    fun status(
        @CurrentMember member: Member,
        @RequestParam endpoint: String
    ): Boolean = webpushService.isSubscribed(member.id, endpoint)

    @PostMapping("/send-notification")
    fun sendNotification(@RequestBody request: TestNotificationRequest): Map<String, Any> =
        webpushService.testSend(request)
}
