package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.MAX_SCRIPT_LINES
import com.github.scottbot95.stationeers.ic.MAX_SCRIPT_LINE_LEN

class ValidationException(message: String? = null) : Exception(message)

// TODO Not sure if this class is really worth it....
open class ValidatingCompilable(private val delegate: Compilable) : Compilable {
    override fun compile(partial: PartialCompiledScript): PartialCompiledScript {
        val nextPartial = delegate.compile(partial)

        if (nextPartial.numLines > MAX_SCRIPT_LINES) {
            throw ValidationException(
                "Compilation results exceeded max line limit($MAX_SCRIPT_LINES). Got: ${nextPartial.numLines}"
            )
        }

        nextPartial.operations.forEachIndexed { i, op ->
            val stringValue = op.toString()
            val lineLen = stringValue.length + 1 // Include newline
            if (lineLen > MAX_SCRIPT_LINE_LEN) {
                throw ValidationException("Line $i exceeded max line limit($MAX_SCRIPT_LINE_LEN). Got: $lineLen")
            }
        }

        return nextPartial
    }
}
