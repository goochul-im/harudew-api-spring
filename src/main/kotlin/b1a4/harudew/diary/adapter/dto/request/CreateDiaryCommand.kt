package b1a4.harudew.diary.adapter.dto.request

import b1a4.harudew.member.domain.Member
import java.time.LocalDate

data class CreateDiaryCommand(
    val author: Member,
    val writtenDate: LocalDate,
    val content: String,
    val weather: String? = null,
    val photoPath: List<String>? = null,
    val audioPath: List<String>? = null,
    val longitude: Number? = null,
    val latitude: Number? = null
)
