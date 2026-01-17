package b1a4.harudew.auth.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT 인증 필터
 *
 * 모든 요청에서 Authorization 헤더의 Bearer 토큰을 확인하고
 * 유효한 토큰이면 SecurityContext에 인증 정보를 설정합니다.
 *
 * 확장 포인트:
 * - 토큰 추출 방식 변경: extractToken() 메서드 수정 (쿠키 등)
 * - 권한 부여 방식 변경: authorities 생성 로직 수정
 * - 특정 경로 제외: shouldNotFilter() 메서드 오버라이드
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractToken(request)

        if (token != null && jwtTokenProvider.validateToken(token)) {
            val memberId = jwtTokenProvider.getMemberIdFromToken(token)
            val socialType = jwtTokenProvider.getSocialTypeFromToken(token)
            val nickname = jwtTokenProvider.getNicknameFromToken(token)

            // 인증 정보 생성
            // 확장: 사용자별 권한이 필요하면 여기서 DB 조회 후 권한 추가
            val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))

            // Principal 객체 생성 (컨트롤러에서 @CurrentMember로 접근 가능)
            val principal = JwtPrincipal(
                memberId = memberId,
                socialType = socialType,
                nickname = nickname
            )

            val authentication = UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities
            )

            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    /**
     * 요청에서 JWT 토큰 추출
     *
     * 기본: Authorization: Bearer {token} 헤더에서 추출
     *
     * 확장: 쿠키에서 토큰을 읽으려면 이 메서드를 수정하세요
     * 예:
     * request.cookies?.find { it.name == "accessToken" }?.value
     */
    private fun extractToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)
        return if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            bearerToken.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }

    /**
     * 특정 경로는 필터를 건너뛰고 싶을 때 오버라이드
     *
     * 예: 인증이 필요없는 정적 리소스 경로 제외
     */
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath
        // 확장: 필터를 건너뛸 경로를 여기에 추가
        return path.startsWith("/api/public/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/")
    }
}

/**
 * JWT 토큰에서 추출한 사용자 정보를 담는 Principal 객체
 *
 * SecurityContext에 저장되어 @CurrentMember 어노테이션으로 접근 가능
 */
data class JwtPrincipal(
    val memberId: String,
    val socialType: b1a4.harudew.member.domain.SocialType,
    val nickname: String
)
