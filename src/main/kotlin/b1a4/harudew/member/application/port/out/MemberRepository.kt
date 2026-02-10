package b1a4.harudew.member.application.port.out

import b1a4.harudew.member.domain.Member

interface MemberRepository {

    fun findById(id: String) : Member

}
