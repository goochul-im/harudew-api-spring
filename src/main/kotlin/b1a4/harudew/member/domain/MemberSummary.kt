package b1a4.harudew.member.domain

import b1a4.harudew.emotion.domain.EmotionSummaryScore
import org.h2.store.Data
import java.time.LocalDate

/**
 * 회원의 특정 날짜에 대한 감정의 집계 도메인
 * 특정 날짜에 여러 감정이 생길 수 있으므로 emotionScore는 배열입니다
 * @param id
 * @param member 회원
 * @param date 집계 날짜
 * @param emotionScore 감정 리스트
 */
class MemberSummary(
    val id: Long,
    val member: Member,
    val date: LocalDate,
    val emotionScore: Array<EmotionSummaryScore>
) {

}
