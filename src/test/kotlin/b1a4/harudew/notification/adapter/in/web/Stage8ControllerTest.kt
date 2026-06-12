package b1a4.harudew.notification.adapter.`in`.web

import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.global.infrastructure.storage.FileUploadRequest
import b1a4.harudew.global.infrastructure.storage.StorageClientPort
import b1a4.harudew.global.infrastructure.storage.UploadController
import b1a4.harudew.member.domain.Member
import b1a4.harudew.member.domain.SocialType
import b1a4.harudew.notification.adapter.dto.response.NotificationResponse
import b1a4.harudew.notification.adapter.dto.response.UnreadNotificationCountResponse
import b1a4.harudew.notification.application.service.NotificationService
import b1a4.harudew.notification.application.service.WebpushService
import b1a4.harudew.notification.domain.NotificationType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.time.LocalDateTime

class Stage8ControllerTest {

    private lateinit var notificationService: NotificationService
    private lateinit var webpushService: WebpushService
    private lateinit var storageClientPort: StorageClientPort
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
        notificationService = mock()
        webpushService = mock()
        storageClientPort = mock()
        mockMvc = MockMvcBuilders
            .standaloneSetup(
                NotificationController(notificationService),
                WebpushController(webpushService),
                UploadController(storageClientPort)
            )
            .setCustomArgumentResolvers(FixedCurrentMemberResolver(member))
            .build()
    }

    @Test
    @DisplayName("GET /noti/all: 프론트 알림 목록 계약을 반환한다")
    fun `notification all returns frontend contract`() {
        whenever(notificationService.findAll(member.id)).thenReturn(
            listOf(
                NotificationResponse(
                    id = 1L,
                    content = "새 일기 분석이 완료되었습니다.",
                    createdAt = LocalDateTime.of(2026, 6, 12, 9, 30),
                    read = false,
                    type = NotificationType.RECAP,
                    diaryId = 10L,
                    photoPath = "https://cdn.test/image.jpg"
                )
            )
        )

        mockMvc.perform(get("/noti/all"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].content").value("새 일기 분석이 완료되었습니다."))
            .andExpect(jsonPath("$[0].read").value(false))
            .andExpect(jsonPath("$[0].type").value("RECAP"))
            .andExpect(jsonPath("$[0].diaryId").value(10))
            .andExpect(jsonPath("$[0].photoPath").value("https://cdn.test/image.jpg"))
    }

    @Test
    @DisplayName("GET /noti/count: 프론트 unread count 계약을 반환한다")
    fun `notification count returns frontend contract`() {
        whenever(notificationService.countUnread(member.id)).thenReturn(UnreadNotificationCountResponse(count = 3))

        mockMvc.perform(get("/noti/count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(3))
    }

    @Test
    @DisplayName("PATCH /noti/{id}: 알림을 읽음 처리한다")
    fun `notification read marks notification as read`() {
        mockMvc.perform(patch("/noti/7"))
            .andExpect(status().isNoContent)

        verify(notificationService).read(member.id, 7L)
    }

    @Test
    @DisplayName("GET /webpush/status: 프론트가 기대하는 raw boolean을 반환한다")
    fun `webpush status returns raw boolean`() {
        whenever(webpushService.isSubscribed(eq(member.id), eq("endpoint-1"))).thenReturn(true)

        mockMvc.perform(get("/webpush/status").param("endpoint", "endpoint-1"))
            .andExpect(status().isOk)
            .andExpect(content().string("true"))
    }

    @Test
    @DisplayName("POST /upload/image: multipart 파일을 업로드하고 imageUrl을 반환한다")
    fun `upload image returns image url`() {
        whenever(storageClientPort.upload(any<FileUploadRequest>())).thenReturn("https://cdn.test/upload.jpg")
        val file = MockMultipartFile(
            "file",
            "photo.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image-bytes".toByteArray()
        )

        mockMvc.perform(multipart("/upload/image").file(file))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.imageUrl").value("https://cdn.test/upload.jpg"))
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
