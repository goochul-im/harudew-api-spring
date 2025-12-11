package b1a4.harudew.emotion.domain

import b1a4.harudew.member.domain.MemberSummary

/**
 * 회원의 특정 감정을 집계할 때 사용됩니다
 * MemberSummary와 N:1로 이어져 있습니다
 * @param id
 * @param summary 회원 요약
 * @param emotion 감정 그룹
 * @param score 점수
 * @param count 집계 횟수
 */
class EmotionSummaryScore(
    val id: Long,
    val summary: MemberSummary,
    val emotion: EmotionGroup,
    val score: Number,
    val count: Number
) {
}
