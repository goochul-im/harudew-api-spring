package b1a4.harudew.member.adapter.out.persistence

import b1a4.harudew.member.application.port.out.MemberRepository
import b1a4.harudew.member.domain.Member
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Repository
class MemberRepositoryImpl : MemberRepository {
    override fun findById(id: String): Member {
        TODO("Not yet implemented")
    }
}
