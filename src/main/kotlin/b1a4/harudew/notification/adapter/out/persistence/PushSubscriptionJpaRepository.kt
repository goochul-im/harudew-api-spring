package b1a4.harudew.notification.adapter.out.persistence

import b1a4.harudew.notification.adapter.out.persistence.entity.PushSubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface PushSubscriptionJpaRepository : JpaRepository<PushSubscriptionEntity, Long> {

    @Query("select p from PushSubscriptionEntity p where p.member.id = :memberId and p.endpoint = :endpoint")
    fun findOwnedByEndpoint(
        @Param("memberId") memberId: String,
        @Param("endpoint") endpoint: String
    ): Optional<PushSubscriptionEntity>

    @Query("select p from PushSubscriptionEntity p where p.isSubscribed = true")
    fun findActive(): List<PushSubscriptionEntity>
}
