package b1a4.harudew.member.adapter.out.persistence

import b1a4.harudew.member.application.port.out.MemberRepository
import b1a4.harudew.member.domain.Member
import b1a4.harudew.global.exception.BusinessException
import b1a4.harudew.global.exception.ErrorCode
import org.springframework.stereotype.Repository

@Repository
class MemberRepositoryImpl(
    private val memberJpaRepository: b1a4.harudew.member.adapter.out.infrastructure.MemberJpaRepository
) : MemberRepository {

    override fun findById(id: String): Member {
        return memberJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }
    }
}
