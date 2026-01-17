package b1a4.harudew.auth.annotation

/**
 * 컨트롤러에서 현재 로그인한 회원의 ID를 주입받기 위한 어노테이션
 *
 * 사용법:
 * ```kotlin
 * @GetMapping("/my-profile")
 * fun getMyProfile(@MemberId memberId: String): ProfileResponse {
 *     return memberService.getProfile(memberId)
 * }
 * ```
 *
 * 주의:
 * - 인증된 요청에서만 사용 가능
 * - 인증되지 않은 요청에서 사용 시 예외 발생
 *
 * 확장: MemberIdArgumentResolver 클래스에서 해석 로직 수정 가능
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class MemberId
