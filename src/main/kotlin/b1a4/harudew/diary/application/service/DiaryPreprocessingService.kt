package b1a4.harudew.diary.application.service

import b1a4.harudew.diary.application.port.`in`.DiaryKeywordPreprocessingCommand
import b1a4.harudew.diary.application.port.`in`.DiaryRagPreprocessingCommand
import b1a4.harudew.diary.application.port.`in`.DiaryPreprocessingUseCase
import b1a4.harudew.diary.application.port.out.chunk.DiaryChunkPort
import b1a4.harudew.diary.application.port.out.embed.SentenceEmbedderPort
import b1a4.harudew.diary.application.port.out.embed.KeywordEmbedderPort
import b1a4.harudew.diary.application.port.out.extract.DiaryKeywordExtractorPort
import b1a4.harudew.diary.application.port.out.tagging.DiaryTaggingPort
import b1a4.harudew.diary.application.port.out.vector.ContentVectorWrapper
import b1a4.harudew.diary.application.port.out.vector.keyword.KeywordVectorPort
import b1a4.harudew.diary.application.port.out.vector.keyword.SaveKeywordRequest
import b1a4.harudew.diary.application.port.out.vector.sentence.SaveSentenceRequest
import b1a4.harudew.diary.application.port.out.vector.sentence.SentenceVectorPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class DiaryPreprocessingService(
    private val diaryChunkPort: DiaryChunkPort,
    private val diaryTaggingPort: DiaryTaggingPort,
    private val sentenceEmbedder: SentenceEmbedderPort,
    private val sentenceVectorPort: SentenceVectorPort,
    private val diaryKeywordExtractor: DiaryKeywordExtractorPort,
    private val keywordEmbedder: KeywordEmbedderPort,
    private val keywordVectorPort: KeywordVectorPort
) : DiaryPreprocessingUseCase {

    override fun ragPreprocessing(command: DiaryRagPreprocessingCommand) {
        val taggedContent = diaryTaggingPort.tag(command.content.trim())
        val chunkedContent = diaryChunkPort.chunk(taggedContent.result)

        val sentences = runBlocking {
            chunkedContent.chunks.map { chunk ->
                async(Dispatchers.IO) {
                    ContentVectorWrapper(chunk, sentenceEmbedder.embedPassage(chunk))
                }
            }.awaitAll()
        }

        sentenceVectorPort.save(SaveSentenceRequest(
            sentences = sentences,
            diaryId = command.diaryId,
            authorId = command.authorId,
            date = command.writtenDate
        ))
    }

    override fun keywordPreprocessing(command: DiaryKeywordPreprocessingCommand) {
        val extractResult = diaryKeywordExtractor.extract(command.content)

        val keywords = runBlocking {
            extractResult.keywords.map { keyword ->
                async(Dispatchers.IO) {
                    ContentVectorWrapper(keyword, keywordEmbedder.embed(keyword))
                }
            }.awaitAll()
        }

        keywordVectorPort.save(SaveKeywordRequest(
            keywords = keywords,
            diaryId = command.diaryId,
            authorId = command.authorId
        ))
    }
}
