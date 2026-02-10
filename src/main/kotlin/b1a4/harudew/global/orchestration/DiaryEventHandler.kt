package b1a4.harudew.global.orchestration

import b1a4.harudew.diary.application.port.`in`.DiaryRagPreprocessingCommand
import b1a4.harudew.diary.application.port.`in`.DiaryPreprocessingUseCase
import b1a4.harudew.diary.domain.event.DiaryCreateEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * Diary에 관련된 부가 관심사를 이벤트 기반으로 분리해둔 클래스입니다
 * 각 이벤트에 맞게 적절한 도메인을 호출합니다
 */
@Component
class DiaryEventHandler(
    private val diaryPreprocessingUseCase: DiaryPreprocessingUseCase
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun createHandler(event: DiaryCreateEvent) {
        diaryPreprocessingUseCase.ragPreprocessing(
            DiaryRagPreprocessingCommand(
                content = event.content,
                diaryId = event.diaryId,
                authorId = event.authorId,
                writtenDate = event.writtenDate
            )
        )
    }

}
