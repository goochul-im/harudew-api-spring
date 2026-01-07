package b1a4.harudew.global.infrastructure.exception

import b1a4.harudew.global.exception.BusinessException
import b1a4.harudew.global.exception.ErrorCode

class ChunkClientException(message: Map<String, Any>?) : BusinessException(ErrorCode.TEXT_PARSING_ERROR, message)
