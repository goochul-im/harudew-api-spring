package b1a4.harudew.auth.security.oauth2

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private val logger = KotlinLogging.logger {}

/**
 * OAuth2 로그인 실패 핸들러
 *
 * 소셜 로그인 실패 시 호출되어 프론트엔드로 에러 정보와 함께 리다이렉트합니다.
 *
 * 실패 원인:
 * - 사용자가 소셜 로그인을 취소
 * - 소셜 로그인 제공자 서버 오류
 * - 사용자 정보 조회 실패
 *
 * 확장 포인트:
 * - 에러 타입별 다른 처리
 * - 실패 로그 기록
 * - 재시도 로직
 */
@Component
class OAuth2FailureHandler(
    private val cookieAuthorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,

    @Value("\${oauth2.failure-redirect-uri:http://localhost:3000/login}")
    private val failureRedirectUri: String
) : SimpleUrlAuthenticationFailureHandler() {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        logger.error("OAuth2 로그인 실패: ${exception.message}", exception)

        // OAuth2 인증 쿠키 정리
        cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response)

        val errorMessage = exception.message ?: "소셜 로그인에 실패했습니다."
        val encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8)

        val targetUrl = UriComponentsBuilder.fromUriString(failureRedirectUri)
            .queryParam("error", "oauth_failed")
            .queryParam("message", encodedMessage)
            .build()
            .toUriString()

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
