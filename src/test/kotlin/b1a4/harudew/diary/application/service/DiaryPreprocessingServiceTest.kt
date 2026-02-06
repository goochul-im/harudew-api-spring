package b1a4.harudew.diary.application.service

import b1a4.harudew.diary.application.port.`in`.DiaryKeywordPreprocessingCommand
import b1a4.harudew.diary.application.port.`in`.DiaryRagPreprocessingCommand
import b1a4.harudew.diary.application.port.out.chunk.ChunkResponse
import b1a4.harudew.diary.application.port.out.chunk.DiaryChunkPort
import b1a4.harudew.diary.application.port.out.embed.KeywordEmbedderPort
import b1a4.harudew.diary.application.port.out.embed.SentenceEmbedderPort
import b1a4.harudew.diary.application.port.out.extract.DiaryKeywordExtractorPort
import b1a4.harudew.diary.application.port.out.extract.KeywordExtractResponse
import b1a4.harudew.diary.application.port.out.tagging.AiDiaryTaggingResponse
import b1a4.harudew.diary.application.port.out.tagging.DiaryTaggingPort
import b1a4.harudew.diary.application.port.out.vector.keyword.KeywordVectorPort
import b1a4.harudew.diary.application.port.out.vector.keyword.SaveKeywordRequest
import b1a4.harudew.diary.application.port.out.vector.sentence.SaveSentenceRequest
import b1a4.harudew.diary.application.port.out.vector.sentence.SentenceVectorPort
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class DiaryPreprocessingServiceTest {

    @Mock lateinit var diaryChunkPort: DiaryChunkPort
    @Mock lateinit var diaryTaggingPort: DiaryTaggingPort
    @Mock lateinit var sentenceEmbedder: SentenceEmbedderPort
    @Mock lateinit var sentenceVectorPort: SentenceVectorPort
    @Mock lateinit var diaryKeywordExtractor: DiaryKeywordExtractorPort
    @Mock lateinit var keywordEmbedder: KeywordEmbedderPort
    @Mock lateinit var keywordVectorPort: KeywordVectorPort

    @InjectMocks
    lateinit var service: DiaryPreprocessingService

    @Test
    @DisplayName("ragPreprocessing: 태깅 → 청킹 → 임베딩 → 벡터 저장 순서로 실행된다")
    fun `ragPreprocessing processes and saves sentence vectors`() {
        // given
        val command = DiaryRagPreprocessingCommand(
            content = "  오늘 날씨가 좋았다  ",
            diaryId = 1L,
            authorId = "user-1",
            writtenDate = LocalDate.of(2025, 1, 1)
        )

        val taggedContent = AiDiaryTaggingResponse(result = "오늘 날씨가 좋았다")
        val chunkedContent = ChunkResponse(chunks = listOf("오늘 날씨가", "좋았다"))
        val vector1 = listOf(0.1, 0.2, 0.3)
        val vector2 = listOf(0.4, 0.5, 0.6)

        `when`(diaryTaggingPort.tag("오늘 날씨가 좋았다")).thenReturn(taggedContent)
        `when`(diaryChunkPort.chunk("오늘 날씨가 좋았다")).thenReturn(chunkedContent)
        `when`(sentenceEmbedder.embedPassage("오늘 날씨가")).thenReturn(vector1)
        `when`(sentenceEmbedder.embedPassage("좋았다")).thenReturn(vector2)

        // when
        service.ragPreprocessing(command)

        // then
        val captor = argumentCaptor<SaveSentenceRequest>()
        verify(sentenceVectorPort).save(captor.capture())

        val saved = captor.firstValue
        assertThat(saved.diaryId).isEqualTo(1L)
        assertThat(saved.authorId).isEqualTo("user-1")
        assertThat(saved.date).isEqualTo(LocalDate.of(2025, 1, 1))
        assertThat(saved.sentences).hasSize(2)
        assertThat(saved.sentences[0].content).isEqualTo("오늘 날씨가")
        assertThat(saved.sentences[0].vector).isEqualTo(vector1)
        assertThat(saved.sentences[1].content).isEqualTo("좋았다")
        assertThat(saved.sentences[1].vector).isEqualTo(vector2)
    }

    @Test
    @DisplayName("ragPreprocessing: content의 앞뒤 공백을 제거한 후 태깅한다")
    fun `ragPreprocessing trims content before tagging`() {
        // given
        val command = DiaryRagPreprocessingCommand(
            content = "  공백 있는 내용  ",
            diaryId = 1L,
            authorId = "user-1",
            writtenDate = LocalDate.of(2025, 1, 1)
        )

        `when`(diaryTaggingPort.tag("공백 있는 내용"))
            .thenReturn(AiDiaryTaggingResponse(result = "공백 있는 내용"))
        `when`(diaryChunkPort.chunk("공백 있는 내용"))
            .thenReturn(ChunkResponse(chunks = listOf("공백 있는 내용")))
        `when`(sentenceEmbedder.embedPassage("공백 있는 내용"))
            .thenReturn(listOf(0.1))

        // when
        service.ragPreprocessing(command)

        // then
        verify(diaryTaggingPort).tag("공백 있는 내용")
    }

    @Test
    @DisplayName("ragPreprocessing: 여러 chunk를 모두 임베딩한다")
    fun `ragPreprocessing embeds all chunks`() {
        // given
        val chunks = listOf("문장1", "문장2", "문장3")
        val command = DiaryRagPreprocessingCommand(
            content = "원본",
            diaryId = 1L,
            authorId = "user-1",
            writtenDate = LocalDate.of(2025, 1, 1)
        )

        `when`(diaryTaggingPort.tag("원본"))
            .thenReturn(AiDiaryTaggingResponse(result = "태깅됨"))
        `when`(diaryChunkPort.chunk("태깅됨"))
            .thenReturn(ChunkResponse(chunks = chunks))
        chunks.forEach { chunk ->
            `when`(sentenceEmbedder.embedPassage(chunk)).thenReturn(listOf(0.1))
        }

        // when
        service.ragPreprocessing(command)

        // then
        chunks.forEach { chunk ->
            verify(sentenceEmbedder).embedPassage(chunk)
        }

        val captor = argumentCaptor<SaveSentenceRequest>()
        verify(sentenceVectorPort).save(captor.capture())
        assertThat(captor.firstValue.sentences).hasSize(3)
    }

    @Test
    @DisplayName("keywordPreprocessing: 키워드 추출 → 임베딩 → 벡터 저장 순서로 실행된다")
    fun `keywordPreprocessing extracts, embeds, and saves keyword vectors`() {
        // given
        val command = DiaryKeywordPreprocessingCommand(
            content = "오늘 카페에서 커피를 마셨다",
            diaryId = 2L,
            authorId = "user-1"
        )

        val extractResult = KeywordExtractResponse(keywords = listOf("카페", "커피"))
        val vector1 = listOf(0.1, 0.2)
        val vector2 = listOf(0.3, 0.4)

        `when`(diaryKeywordExtractor.extract("오늘 카페에서 커피를 마셨다"))
            .thenReturn(extractResult)
        `when`(keywordEmbedder.embed("카페")).thenReturn(vector1)
        `when`(keywordEmbedder.embed("커피")).thenReturn(vector2)

        // when
        service.keywordPreprocessing(command)

        // then
        val captor = argumentCaptor<SaveKeywordRequest>()
        verify(keywordVectorPort).save(captor.capture())

        val saved = captor.firstValue
        assertThat(saved.diaryId).isEqualTo(2L)
        assertThat(saved.authorId).isEqualTo("user-1")
        assertThat(saved.keywords).hasSize(2)
        assertThat(saved.keywords[0].content).isEqualTo("카페")
        assertThat(saved.keywords[0].vector).isEqualTo(vector1)
        assertThat(saved.keywords[1].content).isEqualTo("커피")
        assertThat(saved.keywords[1].vector).isEqualTo(vector2)
    }

    @Test
    @DisplayName("keywordPreprocessing: 추출된 키워드가 없으면 빈 리스트로 저장한다")
    fun `keywordPreprocessing saves empty list when no keywords extracted`() {
        // given
        val command = DiaryKeywordPreprocessingCommand(
            content = "내용",
            diaryId = 1L,
            authorId = "user-1"
        )

        `when`(diaryKeywordExtractor.extract("내용"))
            .thenReturn(KeywordExtractResponse(keywords = emptyList()))

        // when
        service.keywordPreprocessing(command)

        // then
        verify(keywordEmbedder, never()).embed(org.mockito.kotlin.any())

        val captor = argumentCaptor<SaveKeywordRequest>()
        verify(keywordVectorPort).save(captor.capture())
        assertThat(captor.firstValue.keywords).isEmpty()
    }
}
