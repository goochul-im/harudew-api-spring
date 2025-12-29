package b1a4.harudew.member.adapter.dto.response

data class StrengthResponse(
    val typeCount: Map<String, Int>,
    val detailCount: Map<String, Map<String, Int>>
)
