package b1a4.harudew.diary.adapter.dto.response

data class DiaryResponse(
    private val fields: DiaryInfoFields
) : DiaryInfoFields by fields
