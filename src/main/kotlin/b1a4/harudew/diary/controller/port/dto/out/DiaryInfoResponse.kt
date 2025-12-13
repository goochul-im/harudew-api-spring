package b1a4.harudew.diary.controller.port.dto.out

import b1a4.harudew.emotion.port.out.EmotionAnalysisResponse
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class DiaryInfoResponse(
    private val fields: DiaryInfoFields
) : DiaryInfoFields by fields
