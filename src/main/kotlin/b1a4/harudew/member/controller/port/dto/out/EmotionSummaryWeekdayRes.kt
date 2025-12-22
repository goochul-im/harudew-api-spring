package b1a4.harudew.member.controller.port.dto.out

import b1a4.harudew.emotion.domain.EmotionType

data class EmotionSummaryWeekdayRes(
    val monday: List<Emotions>,
    val tuesday: List<Emotions>,
    val wednesday: List<Emotions>,
    val thursday: List<Emotions>,
    val friday: List<Emotions>,
    val saturday: List<Emotions>,
    val sunday: List<Emotions>,
)

data class Emotions(
    val emotion: EmotionType,
    val count: Int
)
