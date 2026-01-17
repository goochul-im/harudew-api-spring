package b1a4.harudew.auth.resolver

import b1a4.harudew.auth.annotation.MemberId
import b1a4.harudew.auth.security.jwt.JwtPrincipal
import b1a4.harudew.global.exception.BusinessException
import b1a4.harudew.global.exception.ErrorCode
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * @MemberId 어노테이션을 처리하는 ArgumentResolver
 *
 * 컨트롤러 메서드의 @MemberId 파라미터에 현재 로그인한 회원의 ID를 주입합니다.
 *
 * 동작 원리:
 * 1. JwtAuthenticationFilter에서 JWT를 파싱하여 SecurityContext에 인증 정보 저장
 * 2. 이 Resolver가 SecurityContext에서 memberId를 추출하여 파라미터에 주입
 *
 * 확장 포인트:
 * - 다른 타입 지원: supportsParameter()에서 추가 타입 체크
 * - 선택적 인증: nullable 파라미터 지원
 */
@Component
class MemberIdArgumentResolver : HandlerMethodArgumentResolver {

    /**
     * 이 Resolver가 처리할 수 있는 파라미터인지 확인
     *
     * @MemberId 어노테이션이 붙은 String 타입 파라미터만 처리
     */
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(MemberId::class.java) &&
                parameter.parameterType == String::class.java
    }

    /**
     * 실제 값을 해석하여 반환
     *
     * SecurityContext에서 현재 인증된 사용자의 memberId를 추출
     */
    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): String {
        val authentication = SecurityContextHolder.getContext().authentication

        // 인증 정보가 없거나 인증되지 않은 경우
        if (authentication == null || !authentication.isAuthenticated) {
            throw BusinessException(ErrorCode.UNAUTHORIZED)
        }

        // Principal에서 memberId 추출
        val principal = authentication.principal

        return when (principal) {
            is JwtPrincipal -> principal.memberId
            // 확장: OAuth2User 등 다른 Principal 타입 지원
            // is CustomOAuth2User -> principal.member.id
            else -> throw BusinessException(ErrorCode.UNAUTHORIZED)
        }
    }
}
