package b1a4.harudew.diary.application.service

import b1a4.harudew.diary.application.port.`in`.DiaryRagPreprocessingCommand
import b1a4.harudew.diary.application.port.`in`.DiaryRagUseCase
import b1a4.harudew.diary.application.port.out.analysis.DiaryAnalysisResponse
import b1a4.harudew.diary.application.port.out.chunk.DiaryChunkPort
import b1a4.harudew.diary.application.port.out.embed.SentenceEmbedderPort
import b1a4.harudew.diary.application.port.out.embed.SimpleEmbedderPort
import b1a4.harudew.diary.application.port.out.tagging.DiaryTaggingPort
import b1a4.harudew.diary.application.port.out.vector.ContentVectorWrapper
import b1a4.harudew.diary.application.port.out.vector.sentence.SaveSentenceRequest
import b1a4.harudew.diary.application.port.out.vector.sentence.SentenceVectorPort
import b1a4.harudew.global.infrastructure.embed.dual.DualEmbedClientPort
import org.springframework.stereotype.Component

@Component
class DiaryRagService(
    private val diaryChunkPort: DiaryChunkPort,
    private val diaryTaggingPort: DiaryTaggingPort,
    private val sentenceEmbedder: SentenceEmbedderPort,
    private val sentenceVectorPort: SentenceVectorPort
) : DiaryRagUseCase {

    override fun preprocessing(command: DiaryRagPreprocessingCommand) {
        val taggedContent = diaryTaggingPort.tag(command.content.trim())
        val chunkedContent = diaryChunkPort.chunk(taggedContent.result)

        sentenceVectorPort.save(SaveSentenceRequest(
            sentences = chunkedContent.chunks.map { chunk ->
                ContentVectorWrapper(
                    chunk,
                    sentenceEmbedder.embedPassage(chunk)
                )
            },
            diaryId = command.diaryId,
            authorId = command.authorId,
            date = command.writtenDate
        ))
    }
}
