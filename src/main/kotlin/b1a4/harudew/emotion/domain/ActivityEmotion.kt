package b1a4.harudew.emotion.domain

import b1a4.harudew.activity.domain.Activity

/**
 * 활동에 나타난 감정
 * @param id
 * @param activity 활동
 * @param emotion 활동에 연관된 감정
 * @param emotionGroup 감정의 그룹
 * @param emotionBase 감정의 base
 * @param intensitySum 이 감정의 합계
 * @param count 이 감정이 나타난 횟수
 */
class ActivityEmotion(
    val id: Long,
    val activity: Activity,
    val emotion: EmotionType,
    val emotionGroup: EmotionGroup,
    val emotionBase: EmotionBase,
    val intensitySum: Double,
    val count: Int
) {
}
