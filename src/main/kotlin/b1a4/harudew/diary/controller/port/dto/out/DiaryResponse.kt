package b1a4.harudew.diary.controller.port.dto.out

data class DiaryResponse(
    private val fields: DiaryInfoFields
) : DiaryInfoFields by fields
