package b1a4.harudew.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus, val code: String, val message: String) {
    // 공통 에러
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "올바르지 않은 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),

    // 인증/인가 에러 (Authentication/Authorization Errors)
    // 새로운 인증 에러 코드 추가 시 A0XX 형식 사용
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A004", "접근 권한이 없습니다."),
    OAUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A005", "소셜 로그인에 실패했습니다."),
    UNSUPPORTED_SOCIAL_TYPE(HttpStatus.BAD_REQUEST, "A006", "지원하지 않는 소셜 로그인 타입입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "A007", "회원을 찾을 수 없습니다."),

    // 도메인 에러
    DIARY_ANALYSIS_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "D001", "AI 분석 중에 오류가 발생했습니다."),
    TEXT_PARSING_ERROR(HttpStatus.BAD_REQUEST, "D002", "텍스트 파싱에 실패했습니다."),
    SEARCH_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "D003", "검색 중 오류가 발생했습니다"),


}
