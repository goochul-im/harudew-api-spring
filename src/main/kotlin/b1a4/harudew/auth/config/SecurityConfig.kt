package b1a4.harudew.auth.config

import b1a4.harudew.auth.security.handler.AccessDeniedHandlerImpl
import b1a4.harudew.auth.security.handler.AuthenticationEntryPointImpl
import b1a4.harudew.auth.security.jwt.JwtAuthenticationFilter
import b1a4.harudew.auth.security.oauth2.CustomOAuth2UserService
import b1a4.harudew.auth.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository
import b1a4.harudew.auth.security.oauth2.OAuth2FailureHandler
import b1a4.harudew.auth.security.oauth2.OAuth2SuccessHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * Spring Security 설정
 *
 * 이 클래스는 애플리케이션의 보안 설정을 담당합니다:
 * - OAuth2 소셜 로그인 (Google, Kakao)
 * - JWT 기반 인증
 * - CORS 설정
 * - 인증/인가 실패 핸들러
 *
 * 확장 포인트:
 * - 새로운 인증 방식 추가: filterChain()에서 설정
 * - 접근 제어 규칙 변경: authorizeHttpRequests() 수정
 * - CORS 설정 변경: corsConfigurationSource() 수정
 * - 새로운 소셜 로그인 추가: application.yml에 provider 추가
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val oAuth2FailureHandler: OAuth2FailureHandler,
    private val authenticationEntryPoint: AuthenticationEntryPointImpl,
    private val accessDeniedHandler: AccessDeniedHandlerImpl,
    private val cookieAuthorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository
) {

    /**
     * Security Filter Chain 설정
     *
     * 확장 포인트:
     * - 특정 경로 인증 제외: permitAll() 추가
     * - 역할 기반 접근 제어: hasRole() 추가
     * - 메서드 보안: @PreAuthorize 활성화
     */
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // CSRF 비활성화 (JWT 사용으로 인해 불필요)
            .csrf { it.disable() }

            // CORS 설정
            .cors { it.configurationSource(corsConfigurationSource()) }

            // 세션 관리: STATELESS (JWT 사용)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

            // 인증/인가 예외 핸들러
            .exceptionHandling {
                it.authenticationEntryPoint(authenticationEntryPoint)  // 401 처리
                it.accessDeniedHandler(accessDeniedHandler)            // 403 처리
            }

            // URL 기반 접근 제어
            .authorizeHttpRequests { auth ->
                auth
                    // 공개 엔드포인트 (인증 불필요)
                    .requestMatchers(
                        "/api/auth/**",           // 인증 관련 API
                        "/auth/**",             // OAuth2 리다이렉트
                        "/login/oauth2/**",       // OAuth2 로그인
                        "/api/public/**",         // 공개 API
                        "/swagger-ui/**",         // Swagger UI
                        "/v3/api-docs/**",        // OpenAPI 스펙
                        "/actuator/health",       // 헬스체크
                        "/error"                  // 에러 페이지
                    ).permitAll()

                    // 그 외 모든 요청은 인증 필요
                    // 확장: 역할 기반 접근 제어 추가 가능
                    // .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }

            // OAuth2 로그인 설정
            .oauth2Login { oauth2 ->
                oauth2
                    // 쿠키 기반 인증 요청 저장소 (STATELESS 세션에서 필수)
                    .authorizationEndpoint {
                        it.authorizationRequestRepository(cookieAuthorizationRequestRepository)
                    }
                    // 커스텀 리다이렉트 엔드포인트 설정
                    // /auth/google/redirect, /auth/kakao/redirect 경로 사용
                    .redirectionEndpoint { it.baseUri("/auth/*/redirect") }
                    // 사용자 정보 로드 서비스 (자동 회원가입 포함)
                    .userInfoEndpoint { it.userService(customOAuth2UserService) }
                    // 로그인 성공 핸들러 (JWT 발급)
                    .successHandler(oAuth2SuccessHandler)
                    // 로그인 실패 핸들러
                    .failureHandler(oAuth2FailureHandler)
            }

            // JWT 인증 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    /**
     * CORS 설정
     *
     * 확장 포인트:
     * - 허용 도메인 추가: allowedOrigins 수정
     * - 허용 헤더 추가: allowedHeaders 수정
     * - 프로파일별 설정: @Profile 사용하여 환경별 CORS 설정
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // 허용할 Origin (프론트엔드 도메인)
        // 확장: 환경변수나 설정 파일에서 읽어오도록 수정 권장
        configuration.allowedOrigins = listOf(
            "http://localhost:3000",       // 로컬 개발 환경
            "http://localhost:5173",       // Vite 개발 서버
            // 확장: 프로덕션 도메인 추가
            // "https://harudew.site",
            // "https://www.harudew.site",
        )

        // 허용할 HTTP 메서드
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")

        // 허용할 헤더
        configuration.allowedHeaders = listOf("*")

        // 인증 정보 포함 허용 (쿠키 등)
        configuration.allowCredentials = true

        // 캐시 시간 (초)
        configuration.maxAge = 3600L

        // 노출할 헤더 (프론트엔드에서 접근 가능)
        configuration.exposedHeaders = listOf("Authorization")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
