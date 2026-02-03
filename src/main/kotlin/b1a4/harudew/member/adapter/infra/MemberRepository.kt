package b1a4.harudew.member.adapter.infra

import b1a4.harudew.member.domain.Member

interface MemberRepository {

    fun findById(id: String) : Member

}
