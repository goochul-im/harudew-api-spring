package b1a4.harudew.auth.annotation

/**
 * 컨트롤러에서 현재 로그인한 회원의 전체 정보를 주입받기 위한 어노테이션
 *
 * 사용법:
 * ```kotlin
 * @GetMapping("/my-diaries")
 * fun getMyDiaries(@CurrentMember member: Member): List<DiaryResponse> {
 *     // member 객체를 직접 사용 - DB 조회 불필요
 *     return diaryService.findByMember(member)
 * }
 * ```
 *
 * @MemberId와의 차이점:
 * - @MemberId: String 타입의 ID만 주입 (DB 조회 필요 없음)
 * - @CurrentMember: Member 도메인 객체 주입 (DB에서 조회)
 *
 * 주의:
 * - DB 조회가 발생하므로 ID만 필요한 경우 @MemberId 사용 권장
 * - 인증되지 않은 요청에서 사용 시 예외 발생
 *
 * 확장: CurrentMemberArgumentResolver 클래스에서 해석 로직 수정 가능
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class CurrentMember
