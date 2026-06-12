package b1a4.harudew.notification.adapter.out.persistence.entity

import b1a4.harudew.member.adapter.out.infrastructure.MemberEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "push_subscription")
class PushSubscriptionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: MemberEntity,

    @Column(nullable = false, columnDefinition = "TEXT")
    val endpoint: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var p256dh: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var auth: String,

    @Column(name = "is_subscribed", nullable = false)
    var isSubscribed: Boolean = true
)
