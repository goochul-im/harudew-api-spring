package b1a4.harudew.diary.application.service

import b1a4.harudew.diary.adapter.dto.request.CreateDiaryCommand
import b1a4.harudew.diary.adapter.dto.response.DiaryAnalysisResult
import b1a4.harudew.diary.adapter.dto.response.DiaryDetailResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryPageResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryResponse
import b1a4.harudew.diary.adapter.dto.response.DiarySearchResponse
import b1a4.harudew.diary.application.port.out.DiaryRepository
import b1a4.harudew.diary.application.port.`in`.DiaryCommandUseCase
import b1a4.harudew.diary.application.port.`in`.DiaryQueryUseCase
import b1a4.harudew.diary.application.port.out.analysis.DiaryAnalysisPort
import b1a4.harudew.diary.application.port.out.analysis.DiaryAnalysisResponse
import b1a4.harudew.diary.domain.event.DiaryCreateEvent
import b1a4.harudew.diary.domain.model.Diary
import b1a4.harudew.global.event.DomainEventPublisherPort
import b1a4.harudew.global.infrastructure.storage.FileUploadRequest
import b1a4.harudew.global.infrastructure.storage.StorageClientPort
import b1a4.harudew.member.application.port.out.MemberRepository
import b1a4.harudew.member.domain.Member
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@Service
class DiaryService(
    private val storageClient: StorageClientPort,
    private val diaryAnalysisAdapter: DiaryAnalysisPort,
    private val eventPublisher: DomainEventPublisherPort,
    private val diaryRepository: DiaryRepository,
    private val memberRepository: MemberRepository
) : DiaryCommandUseCase, DiaryQueryUseCase {

    override suspend fun create(authorId: String, command: CreateDiaryCommand, photos: List<MultipartFile>?) {
        val (photoUrls, analysisResult) = coroutineScope {
            val photos = async { uploadFiles(photos) }
            val diaryAnalysisResult = async { analysisDiary(command.content) }
            photos.await() to diaryAnalysisResult.await()
        }

        val author = memberRepository.findById(authorId)
        val result = Diary(
            author = author,
            writtenDate = LocalDate.now(),
            photoPath = photoUrls,
            latitude = command.latitude,
            longitude = command.longitude,
            audioPath = emptyList(),
            metaData = analysisResult,
            content = command.content,
        )

        val saveDiary = diaryRepository.save(result)
        eventPublisher.publish(
            DiaryCreateEvent(
                saveDiary.id!!,
                analysisResult
            )
        )
    }

    override fun delete(diaryId: Long) {
        TODO("Not yet implemented")
    }

    override fun toggleBookmark(diaryId: Long) {
        TODO("Not yet implemented")
    }

    override fun findById(
        diaryId: Long,
        member: Member
    ): DiaryAnalysisResult {
        TODO("Not yet implemented")
    }

    override fun findJsonById(
        diaryId: Long,
        member: Member
    ): DiaryDetailResponse {
        TODO("Not yet implemented")
    }

    override fun findInfoByDate(
        diaryId: Long,
        member: Member,
        date: LocalDate
    ): DiaryResponse {
        TODO("Not yet implemented")
    }

    override fun search(
        member: Member,
        query: String
    ): DiarySearchResponse {
        TODO("Not yet implemented")
    }

    override fun findAll(member: Member): DiaryPageResponse {
        TODO("Not yet implemented")
    }

    override fun findBookmark(member: Member): DiaryPageResponse {
        TODO("Not yet implemented")
    }

    private suspend fun analysisDiary(content: String): DiaryAnalysisResponse =
        withContext(Dispatchers.IO) {
            diaryAnalysisAdapter.getAnalysis(content)
        }

    /**
     * 코루틴을 이용해 여러 파일을 병렬로 업로드
     */
    private suspend fun uploadFiles(files: List<MultipartFile>?): List<String> =
        coroutineScope {
            files?.map { file ->
                async(Dispatchers.IO) {
                    storageClient.upload(
                        FileUploadRequest(
                            bytes = file.bytes,
                            contentType = file.contentType ?: "application/octet-stream",
                            originalFilename = file.originalFilename,
                        )
                    )
                }
            }?.awaitAll() ?: emptyList()
        }
}
