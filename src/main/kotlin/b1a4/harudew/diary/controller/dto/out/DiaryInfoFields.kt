package b1a4.harudew.diary.controller.dto.out

import java.time.LocalDate

interface DiaryInfoFields {
    val diaryId: Long
    val title: String
    val writtenDate: LocalDate
    val content: String
    val photoPath: List<String>?
    val audioPath: List<String>?
    val isBookmarked: Boolean
    val latitude: Number
    val longitude: Number
    val activities: List<String>?
    val emotions: List<JsonEmotionResponse>?
    val people: List<String>?
}
