package b1a4.harudew.member.adapter.infra

import org.springframework.data.jpa.repository.JpaRepository

interface MemberJpaRepository : JpaRepository<MemberEntity, String> {
}
