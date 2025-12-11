package b1a4.harudew.person.domain

/**
 * 별칭 도메인
 * 한 사람이 여러 별칭을 가질 수 있습니다
 * @param id
 * @param name 별명
 * @param person 이 별명을 가진 사람
 */
class Alias(
    val id: Long,
    val name: String,
    val person: Person
)
