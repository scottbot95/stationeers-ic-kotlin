package com.github.scottbot95.stationeers.ic.simulation

open class ICScriptRuntimeException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {

}

class ICScriptYieldException : ICScriptRuntimeException("Execution yielded by script")