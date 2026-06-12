package b1a4.harudew.notification.adapter.out.persistence

import b1a4.harudew.notification.adapter.out.persistence.entity.NotificationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface NotificationJpaRepository : JpaRepository<NotificationEntity, Long> {

    @Query("select n from NotificationEntity n where n.member.id = :memberId order by n.createdAt desc, n.id desc")
    fun findAllByMember(@Param("memberId") memberId: String): List<NotificationEntity>

    @Query("select n from NotificationEntity n where n.member.id = :memberId and n.read = false order by n.createdAt desc, n.id desc")
    fun findUnreadByMember(@Param("memberId") memberId: String): List<NotificationEntity>

    @Query("select count(n) from NotificationEntity n where n.member.id = :memberId and n.read = false")
    fun countUnreadByMember(@Param("memberId") memberId: String): Int

    @Query("select n from NotificationEntity n where n.id = :id and n.member.id = :memberId")
    fun findOwnedById(
        @Param("id") id: Long,
        @Param("memberId") memberId: String
    ): Optional<NotificationEntity>
}
