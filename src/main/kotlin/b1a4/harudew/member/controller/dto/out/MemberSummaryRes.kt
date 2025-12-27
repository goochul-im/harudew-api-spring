package b1a4.harudew.member.controller.dto.out

import b1a4.harudew.emotion.domain.EmotionGroup
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class MemberSummaryRes(
    val depressionWarning: Boolean,
    val stressWarning: Boolean,
    val anxietyWarning: Boolean,
    val period: Int,
    @JsonProperty("emotionPerDate")
    val emotionPerDate: List<PerDate>
)

data class PerDate(
    val date: LocalDate,
    val emotions: List<EmotionGroups>
)

data class EmotionGroups(
    val emotion: EmotionGroup,
    val intensity: Number
)
