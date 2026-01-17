package b1a4.harudew.auth.resolver

import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.auth.security.jwt.JwtPrincipal
import b1a4.harudew.global.exception.BusinessException
import b1a4.harudew.global.exception.ErrorCode
import b1a4.harudew.member.adapter.infra.MemberJpaRepository
import b1a4.harudew.member.domain.Member
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * @CurrentMember 어노테이션을 처리하는 ArgumentResolver
 *
 * 컨트롤러 메서드의 @CurrentMember 파라미터에 현재 로그인한 회원의 Member 객체를 주입합니다.
 *
 * 동작 원리:
 * 1. SecurityContext에서 memberId 추출
 * 2. DB에서 Member 정보 조회
 * 3. Member 도메인 객체를 파라미터에 주입
 *
 * 주의:
 * - 매 요청마다 DB 조회가 발생합니다
 * - ID만 필요한 경우 @MemberId 사용을 권장합니다
 *
 * 확장 포인트:
 * - 캐싱: 자주 조회되는 회원 정보 캐시
 * - 추가 정보 로딩: 연관 엔티티 함께 조회
 */
@Component
class CurrentMemberArgumentResolver(
    private val memberRepository: MemberJpaRepository
) : HandlerMethodArgumentResolver {

    /**
     * 이 Resolver가 처리할 수 있는 파라미터인지 확인
     *
     * @CurrentMember 어노테이션이 붙은 Member 타입 파라미터만 처리
     */
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentMember::class.java) &&
                parameter.parameterType == Member::class.java
    }

    /**
     * 실제 값을 해석하여 반환
     *
     * SecurityContext에서 memberId를 추출하고 DB에서 Member 조회
     */
    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Member {
        val authentication = SecurityContextHolder.getContext().authentication

        // 인증 정보가 없거나 인증되지 않은 경우
        if (authentication == null || !authentication.isAuthenticated) {
            throw BusinessException(ErrorCode.UNAUTHORIZED)
        }

        val memberId = when (val principal = authentication.principal) {
            is JwtPrincipal -> principal.memberId
            else -> throw BusinessException(ErrorCode.UNAUTHORIZED)
        }

        // DB에서 회원 조회
        // 확장: 캐싱이 필요하면 @Cacheable 추가 또는 캐시 서비스 사용
        return memberRepository.findById(memberId)
            .map { it.toDomain() }
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }
    }
}
