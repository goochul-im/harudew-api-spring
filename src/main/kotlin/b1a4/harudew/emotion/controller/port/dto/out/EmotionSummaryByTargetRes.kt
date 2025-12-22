package b1a4.harudew.emotion.controller.port.dto.out

import b1a4.harudew.emotion.domain.EmotionType
import b1a4.harudew.member.controller.port.dto.out.Emotions
import java.time.LocalDate

data class EmotionSummaryByTargetRes(
    val date: LocalDate,
    val emotions: List<EmotionDetailRes>
)

data class EmotionDetailRes(
    val emotion: EmotionType,
    val count: Int,
    val intensity: Number
)
