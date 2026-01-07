package b1a4.harudew.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus, val code: String, val message: String) {
    // 공통 에러
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "올바르지 않은 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),

    // 도메인 에러
    DIARY_ANALYSIS_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "D001", "AI 분석 중에 오류가 발생했습니다."),
    TEXT_PARSING_ERROR(HttpStatus.BAD_REQUEST, "D002", "텍스트 파싱에 실패했습니다.");
}
