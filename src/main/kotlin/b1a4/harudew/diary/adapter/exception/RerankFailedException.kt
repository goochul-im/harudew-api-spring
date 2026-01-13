package b1a4.harudew.diary.adapter.exception

import b1a4.harudew.global.exception.BusinessException
import b1a4.harudew.global.exception.ErrorCode

class RerankFailedException(cause: Throwable, detail: Map<String, Any>) : BusinessException(ErrorCode.SEARCH_FAILED, cause, detail)
