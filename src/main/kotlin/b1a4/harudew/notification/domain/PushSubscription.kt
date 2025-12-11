package b1a4.harudew.notification.domain

import b1a4.harudew.member.domain.Member

class PushSubscription(
    val id: Long,
    val endpoint: String,
    val p256dh: String,
    val auth: String,
    val author: Member,
    val isSubscribed: Boolean = false
) {
}
