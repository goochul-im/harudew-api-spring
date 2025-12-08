package b1a4.harudew.target.domain

import b1a4.harudew.diary.domain.Diary

/**
 * 일기에 나타난 인물 매핑 도메인입니다
 * 인물은 여러 일기에 반복적으로 나타날 수 있습니다
 * @param id
 * @param diary 일기
 * @param person 일기에 나타난 인물
 * @param changeScore 일기에서 나타난 인물에 대한 변동 점수
 */
class DiaryPerson(
    val id: Long,
    val diary: Diary,
    val person: Person,
    val changeScore: Float
)
