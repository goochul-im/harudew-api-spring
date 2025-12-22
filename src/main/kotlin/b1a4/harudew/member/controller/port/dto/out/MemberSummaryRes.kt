package b1a4.harudew.member.controller.port.dto.out

import b1a4.harudew.emotion.domain.EmotionGroup

data class MemberSummaryRes(
    val depressionWarning: Boolean,
    val stressWarning: Boolean,
    val anxietyWarning: Boolean,
    val period: Int,
    val emotions: List<EmotionGroups>
)

data class EmotionGroups(
    val emotion: EmotionGroup,
    val intensity: Number
)
