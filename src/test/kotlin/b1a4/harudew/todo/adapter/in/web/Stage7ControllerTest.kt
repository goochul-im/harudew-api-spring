package b1a4.harudew.todo.adapter.`in`.web

import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.member.domain.Member
import b1a4.harudew.member.domain.SocialType
import b1a4.harudew.recommend.adapter.dto.response.RecommendVideoResponse
import b1a4.harudew.recommend.adapter.dto.response.RoutineResponse
import b1a4.harudew.recommend.adapter.`in`.web.RecommendController
import b1a4.harudew.recommend.adapter.`in`.web.RoutineController
import b1a4.harudew.recommend.application.service.RecommendService
import b1a4.harudew.recommend.application.service.RoutineService
import b1a4.harudew.recommend.domain.RoutineType
import b1a4.harudew.todo.adapter.dto.response.TodoCalendarResponse
import b1a4.harudew.todo.application.service.TodoService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.time.LocalDate

class Stage7ControllerTest {

    private lateinit var todoService: TodoService
    private lateinit var routineService: RoutineService
    private lateinit var recommendService: RecommendService
    private lateinit var mockMvc: MockMvc

    private val member = Member(
        id = "member-1",
        email = "member@test.local",
        nickname = "테스터",
        socialType = SocialType.GOOGLE,
        character = "test"
    )

    @BeforeEach
    fun setUp() {
        todoService = mock()
        routineService = mock()
        recommendService = mock()
        mockMvc = MockMvcBuilders
            .standaloneSetup(
                TodoController(todoService),
                RoutineController(routineService),
                RecommendController(recommendService)
            )
            .setCustomArgumentResolvers(FixedCurrentMemberResolver(member))
            .build()
    }

    @Test
    @DisplayName("POST /todos/calendar: 프론트 Todo 생성 계약을 반환한다")
    fun `create calendar todo returns frontend contract`() {
        whenever(todoService.createCalendar(eq(member.id), any())).thenReturn(
            TodoCalendarResponse(
                id = 1L,
                content = "산책하기",
                isComplete = false,
                date = LocalDate.of(2026, 6, 12)
            )
        )

        mockMvc.perform(
            post("/todos/calendar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"산책하기","date":"2026-06-12"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.content").value("산책하기"))
            .andExpect(jsonPath("$.isComplete").value(false))
            .andExpect(jsonPath("$.date").value("2026-06-12"))
    }

    @Test
    @DisplayName("GET /routine/{type}: lowercase routine type 경로를 지원한다")
    fun `routine type supports frontend lowercase path`() {
        whenever(routineService.findByType(member.id, RoutineType.STRESS)).thenReturn(
            listOf(RoutineResponse(1L, "호흡하기", RoutineType.STRESS, false))
        )

        mockMvc.perform(get("/routine/stress"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].routineId").value(1))
            .andExpect(jsonPath("$[0].routineType").value("stress"))
    }

    @Test
    @DisplayName("GET /recommend/video: 영상 추천 fallback 계약을 반환한다")
    fun `recommend video returns frontend contract`() {
        whenever(recommendService.video(member.id, 10)).thenReturn(
            RecommendVideoResponse(
                videoId = listOf("inpok4MKVLM"),
                emotion = "평온",
                message = "10일 내의 평온 추천 영상입니다"
            )
        )

        mockMvc.perform(get("/recommend/video").param("period", "10"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.videoId[0]").value("inpok4MKVLM"))
            .andExpect(jsonPath("$.emotion").value("평온"))
    }

    private class FixedCurrentMemberResolver(
        private val member: Member
    ) : HandlerMethodArgumentResolver {

        override fun supportsParameter(parameter: MethodParameter): Boolean =
            parameter.hasParameterAnnotation(CurrentMember::class.java)

        override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?
        ): Any = member
    }
}
