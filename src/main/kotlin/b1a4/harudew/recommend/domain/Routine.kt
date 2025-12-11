package b1a4.harudew.recommend.domain

import b1a4.harudew.member.domain.Member

/**
 * 추천 루틴 도메인입니다
 * @param id
 * @param routineType 루틴 타입 (STRESS, DEPRESSION, ANXIETY)
 * @param content 내용
 * @param isTrigger 사용자가 이 루틴을 사용할지 선택 여부
 * @param author 사용지
 */
class Routine(
    val id: Long,
    val routineType: RoutineType,
    val content: String,
    val isTrigger: Boolean,
    val author: Member
) {
}

enum class RoutineType {
    STRESS,
    DEPRESSION,
    ANXIETY,
}
