package b1a4.harudew.diary.application.service

import b1a4.harudew.diary.application.port.`in`.DiaryKeywordPreprocessingCommand
import b1a4.harudew.diary.application.port.`in`.DiaryPreprocessingUseCase
import b1a4.harudew.diary.application.port.`in`.DiaryRagPreprocessingCommand
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("local")
class LocalDiaryPreprocessingService : DiaryPreprocessingUseCase {

    override fun ragPreprocessing(command: DiaryRagPreprocessingCommand) {
        // local 통합 검증에서는 외부 tagging/embed/Qdrant 호출을 수행하지 않는다.
    }

    override fun keywordPreprocessing(command: DiaryKeywordPreprocessingCommand) {
        // local 통합 검증에서는 외부 keyword/embed/Qdrant 호출을 수행하지 않는다.
    }
}
