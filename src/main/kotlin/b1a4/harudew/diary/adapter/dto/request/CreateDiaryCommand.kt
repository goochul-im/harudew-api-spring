package b1a4.harudew.diary.adapter.dto.request

import java.time.LocalDate

data class CreateDiaryCommand(
    val writtenDate: LocalDate,
    val content: String,
    val weather: String = "NONE",
    val longitude: Double? = null,
    val latitude: Double? = null
)
