package b1a4.harudew.auth.config

import b1a4.harudew.auth.resolver.CurrentMemberArgumentResolver
import b1a4.harudew.auth.resolver.MemberIdArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Spring MVC 설정
 *
 * 컨트롤러에서 사용할 커스텀 ArgumentResolver를 등록합니다.
 *
 * 등록된 Resolver:
 * - MemberIdArgumentResolver: @MemberId 어노테이션 처리
 * - CurrentMemberArgumentResolver: @CurrentMember 어노테이션 처리
 *
 * 확장 포인트:
 * - 새로운 ArgumentResolver 추가: addArgumentResolvers()에 추가
 * - 인터셉터 추가: addInterceptors() 오버라이드
 * - 리소스 핸들러 추가: addResourceHandlers() 오버라이드
 */
@Configuration
class WebMvcConfig(
    private val memberIdArgumentResolver: MemberIdArgumentResolver,
    private val currentMemberArgumentResolver: CurrentMemberArgumentResolver
) : WebMvcConfigurer {

    /**
     * 커스텀 ArgumentResolver 등록
     *
     * 확장: 새로운 어노테이션 기반 파라미터 바인딩이 필요하면 여기에 Resolver 추가
     */
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(memberIdArgumentResolver)
        resolvers.add(currentMemberArgumentResolver)
        // 확장: 추가 Resolver 등록
        // resolvers.add(customArgumentResolver)
    }
}
