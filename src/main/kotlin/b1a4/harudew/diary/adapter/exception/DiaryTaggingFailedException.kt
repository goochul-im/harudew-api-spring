package b1a4.harudew.diary.adapter.exception

import b1a4.harudew.global.exception.BusinessException
import b1a4.harudew.global.exception.ErrorCode

class DiaryTaggingFailedException(cause: Throwable, detail: Map<String, Any>) : BusinessException(ErrorCode.DIARY_ANALYSIS_FAILED, cause, detail)
