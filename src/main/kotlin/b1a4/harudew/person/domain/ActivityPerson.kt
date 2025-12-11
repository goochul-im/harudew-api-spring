package b1a4.harudew.person.domain

import b1a4.harudew.activity.domain.Activity

// 정말 필요한가??
class ActivityPerson(
    val id: Long,
    val activity: Activity,
    val target: Person
) {
}
