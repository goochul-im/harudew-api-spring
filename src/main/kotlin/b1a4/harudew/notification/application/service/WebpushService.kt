package b1a4.harudew.notification.application.service

import b1a4.harudew.member.adapter.out.infrastructure.MemberJpaRepository
import b1a4.harudew.notification.adapter.out.persistence.PushSubscriptionJpaRepository
import b1a4.harudew.notification.adapter.out.persistence.entity.PushSubscriptionEntity
import org.springframework.stereotype.Service

@Service
class WebpushService(
    private val pushSubscriptionJpaRepository: PushSubscriptionJpaRepository,
    private val memberJpaRepository: MemberJpaRepository
) {

    fun subscribe(memberId: String, request: WebpushSubscriptionRequest) {
        val existing = pushSubscriptionJpaRepository.findOwnedByEndpoint(memberId, request.endpoint)
        val entity = existing.orElseGet {
            PushSubscriptionEntity(
                member = memberJpaRepository.getReferenceById(memberId),
                endpoint = request.endpoint,
                p256dh = request.keys.p256dh,
                auth = request.keys.auth
            )
        }
        entity.p256dh = request.keys.p256dh
        entity.auth = request.keys.auth
        entity.isSubscribed = true
        pushSubscriptionJpaRepository.save(entity)
    }

    fun unsubscribe(memberId: String, endpoint: String) {
        val existing = pushSubscriptionJpaRepository.findOwnedByEndpoint(memberId, endpoint)
        if (existing.isPresent) {
            val entity = existing.get()
            entity.isSubscribed = false
            pushSubscriptionJpaRepository.save(entity)
        }
    }

    fun isSubscribed(memberId: String, endpoint: String): Boolean =
        pushSubscriptionJpaRepository.findOwnedByEndpoint(memberId, endpoint)
            .map { it.isSubscribed }
            .orElse(false)

    fun testSend(payload: TestNotificationRequest): Map<String, Any> =
        mapOf(
            "sent" to pushSubscriptionJpaRepository.findActive().size,
            "title" to payload.title,
            "body" to payload.body
        )
}

data class WebpushSubscriptionRequest(
    val endpoint: String,
    val expirationTime: Long? = null,
    val keys: WebpushKeys
)

data class WebpushKeys(
    val p256dh: String,
    val auth: String
)

data class UnsubscribeRequest(
    val endpoint: String
)

data class TestNotificationRequest(
    val title: String,
    val body: String,
    val options: Map<String, Any>? = null
)
