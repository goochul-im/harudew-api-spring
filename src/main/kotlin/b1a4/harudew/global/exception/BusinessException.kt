package b1a4.harudew.global.exception

open class BusinessException(val errorCode: ErrorCode, cause: Throwable? = null, val detail: Map<String, Any>? = null) : RuntimeException(errorCode.message, cause)
