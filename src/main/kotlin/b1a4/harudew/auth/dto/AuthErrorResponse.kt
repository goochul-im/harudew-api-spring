package b1a4.harudew.auth.dto

import b1a4.harudew.global.exception.ErrorCode
import java.time.LocalDateTime

/**
 * 인증/인가 실패 시 응답 DTO
 *
 * 표준 에러 응답 형식을 따르며, 프론트엔드에서 일관된 에러 처리가 가능합니다.
 *
 * 확장 포인트:
 * - 필드 추가: 새로운 에러 정보 필드 추가 (예: errorId, traceId)
 * - 형식 변경: of() 팩토리 메서드 수정
 */
data class AuthErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val code: String,
    val message: String,
    val detail: String? = null
) {
    companion object {
        /**
         * ErrorCode로부터 AuthErrorResponse 생성
         *
         * @param errorCode 에러 코드 enum
         * @param detail 추가 상세 정보 (선택)
         */
        fun of(errorCode: ErrorCode, detail: String? = null): AuthErrorResponse {
            return AuthErrorResponse(
                timestamp = LocalDateTime.now(),
                status = errorCode.status.value(),
                code = errorCode.code,
                message = errorCode.message,
                detail = detail
            )
        }

        /**
         * OAuth 인증 실패 응답 생성
         *
         * @param socialType 소셜 로그인 타입 (google, kakao)
         * @param reason 실패 사유
         */
        fun oauthFailed(socialType: String, reason: String): AuthErrorResponse {
            return AuthErrorResponse(
                timestamp = LocalDateTime.now(),
                status = ErrorCode.OAUTH_AUTHENTICATION_FAILED.status.value(),
                code = ErrorCode.OAUTH_AUTHENTICATION_FAILED.code,
                message = "${socialType} 로그인에 실패했습니다.",
                detail = reason
            )
        }
    }
}
