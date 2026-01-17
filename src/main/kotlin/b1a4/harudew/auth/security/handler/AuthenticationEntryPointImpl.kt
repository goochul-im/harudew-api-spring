package b1a4.harudew.auth.security.handler

import b1a4.harudew.auth.dto.AuthErrorResponse
import b1a4.harudew.global.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

/**
 * 인증 실패 핸들러 (401 Unauthorized)
 *
 * 인증되지 않은 사용자가 보호된 리소스에 접근하려 할 때 호출됩니다.
 *
 * 발생 케이스:
 * - 토큰 없이 보호된 API 접근
 * - 유효하지 않은 토큰으로 접근
 * - 만료된 토큰으로 접근
 *
 * 확장 포인트:
 * - 응답 형식 변경: commence() 메서드의 응답 로직 수정
 * - 로깅 추가: 인증 실패 시 로그 기록
 * - 에러 세분화: request.getAttribute()로 에러 타입 구분
 */
@Component
class AuthenticationEntryPointImpl(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val errorCode = determineErrorCode(request, authException)

        response.status = errorCode.status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse = AuthErrorResponse.of(errorCode, authException.message)

        // 확장: 로깅이 필요하면 여기에 추가
        // logger.warn("Authentication failed: ${authException.message}")

        objectMapper.writeValue(response.writer, errorResponse)
    }

    /**
     * 에러 코드 결정
     *
     * 확장: 더 세분화된 에러 처리가 필요하면 이 메서드를 수정하세요
     * 예: 토큰 만료와 유효하지 않은 토큰을 구분
     */
    private fun determineErrorCode(
        request: HttpServletRequest,
        authException: AuthenticationException
    ): ErrorCode {
        // JwtAuthenticationFilter에서 설정한 에러 타입 확인
        val errorType = request.getAttribute("errorType") as? String

        return when (errorType) {
            "EXPIRED" -> ErrorCode.EXPIRED_TOKEN
            "INVALID" -> ErrorCode.INVALID_TOKEN
            else -> ErrorCode.UNAUTHORIZED
        }
    }
}
