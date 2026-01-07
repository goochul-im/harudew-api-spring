package b1a4.harudew.global.exception

data class ErrorResponse(
    val code: String,       // 프론트가 식별할 에러 코드")
    val message: String,    // 사용자에게 보여줄 상세 메시지
    val detail: String? = null // 디버깅용 상세 정보
)
