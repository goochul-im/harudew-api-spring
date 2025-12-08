package b1a4.harudew.target.domain

import b1a4.harudew.member.domain.Member
import java.time.LocalDate

/**
 * @param id
 * @param name 이름
 * @param count 등장한 횟수
 * @param recentDate 가장 최근에 등장한 날
 * @param relation 관계 타입 (enum)
 * @param type 인물 타입 (enum)
 * @param affection 심적 거리
 * @param closenessScore 친밀도 점수
 */
class Person(
    val id: Long,
    val name: String,
    val count: Int,
    val recentDate: LocalDate,
    val relation: PersonRelation = PersonRelation.ETC,
    val type: PersonType = PersonType.PERSON,
    val affection : Number,
    val closenessScore: Number,
    val member: Member,
)

enum class PersonRelation {
    FAMILY, FRIEND, LOVER, SUPERIOR, COLLEAGUE, ACQUAINTANCE, ETC
}

enum class PersonType {
    PERSON
}
