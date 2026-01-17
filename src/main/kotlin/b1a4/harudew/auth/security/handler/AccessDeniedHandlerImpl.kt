package b1a4.harudew.auth.security.handler

import b1a4.harudew.auth.dto.AuthErrorResponse
import b1a4.harudew.global.exception.ErrorCode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

/**
 * 인가 실패 핸들러 (403 Forbidden)
 *
 * 인증된 사용자가 권한이 없는 리소스에 접근하려 할 때 호출됩니다.
 *
 * 발생 케이스:
 * - 일반 사용자가 관리자 전용 API 접근
 * - 다른 사용자의 리소스에 접근 시도
 *
 * 확장 포인트:
 * - 응답 형식 변경: handle() 메서드의 응답 로직 수정
 * - 로깅 추가: 권한 거부 시 로그 기록 (보안 감사용)
 * - 리소스별 메시지: 접근하려던 리소스 정보 포함
 */
@Component
class AccessDeniedHandlerImpl(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        val errorCode = ErrorCode.ACCESS_DENIED

        response.status = errorCode.status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse = AuthErrorResponse.of(
            errorCode = errorCode,
            detail = "요청한 리소스에 대한 접근 권한이 없습니다: ${request.requestURI}"
        )

        // 확장: 보안 감사 로깅이 필요하면 여기에 추가
        // logger.warn("Access denied for user ${SecurityContextHolder.getContext().authentication?.name} to ${request.requestURI}")

        objectMapper.writeValue(response.writer, errorResponse)
    }
}
