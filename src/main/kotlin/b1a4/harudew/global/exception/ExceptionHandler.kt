package b1a4.harudew.global.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {

    private val log = KotlinLogging.logger { }

    @org.springframework.web.bind.annotation.ExceptionHandler(BusinessException::class)
    protected fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {

        val element = e.stackTrace.firstOrNull { it.className.startsWith("b1a4.harudew") }
        val location = element?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"

        log.error {
            "비즈니스 예외 발생\n" +
                    "위치 : $location\n" +
                    "비즈니스 메시지 : ${e.message}\n" +
                    "실제 에러 원인 : ${e.cause?.message ?: "상세 원인 없음"}\n" +
                    "파라미터 : ${e.detail ?: ""}"
        }

        val response = ErrorResponse(
            code = e.errorCode.code, message = e.errorCode.message
        )

        return ResponseEntity(response, e.errorCode.status)
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception::class)
    protected fun handleException(e: Exception): ResponseEntity<ErrorResponse> {

        val element = e.stackTrace.firstOrNull { it.className.startsWith("b1a4.harudew") }
        val location = element?.let { "${it.className}.${it.methodName}:${it.lineNumber}" } ?: "Unknown"

        log.error {
            "비즈니스 예외 발생\n" +
                    "위치 : $location\n" +
                    "비즈니스 메시지 : ${e.message}\n" +
                    "실제 에러 원인 : ${e.cause?.message ?: "상세 원인 없음"}\n"
        }

        val response = ErrorResponse(
            code = ErrorCode.INTERNAL_SERVER_ERROR.code,
            message = ErrorCode.INTERNAL_SERVER_ERROR.message,
            detail = e.message // 운영 환경에서는 보안상 생략하는 것이 좋습니다.
        )
        return ResponseEntity(response, ErrorCode.INTERNAL_SERVER_ERROR.status)
    }

}
