package b1a4.harudew.person.domain

import b1a4.harudew.emotion.domain.EmotionType
import java.time.LocalDate

/**
 * 특정 인물에게 특정 날짜에 느낀 감정을 집계합니다
 * @param id
 * @param emotion 느낀 감정
 * @param person 인물
 * @param intensity 강도
 * @param count 집계 횟수
 * @param feelDate 감정을 느낀 날짜
 */
class EmotionPerson(
    val id: Long,
    val emotion: EmotionType,
    val person: Person,
    val intensity: Float,
    val count: Int,
    val feelDate: LocalDate
) {
}
