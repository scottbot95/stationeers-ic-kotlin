package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.MAX_SCRIPT_LINES
import com.github.scottbot95.stationeers.ic.MAX_SCRIPT_LINE_LEN

class ValidationException(message: String? = null) : Exception(message)

// TODO Not sure if this class is really worth it....
open class ValidatingCompilable(private val delegate: Compilable) : Compilable {
    override fun compile(context: CompileContext): CompileResults {
        val result = delegate.compile(context)

        if (result.lines.size > MAX_SCRIPT_LINES) {
            throw ValidationException(
                "Compilation results exceeded max line limit($MAX_SCRIPT_LINES). Got: ${result.lines.size}"
            )
        }
        result.lines.forEachIndexed { i, it ->
            // I think technically the last line can be one longer, but we're gonna ignore that (:
            val line = it.toString(context + i)
            val lineLen = line.length + 1
            if (lineLen > MAX_SCRIPT_LINE_LEN) {
                throw ValidationException("Line $i exceeded max line limit($MAX_SCRIPT_LINE_LEN). Got: $lineLen")
            }
        }

        return result
    }
}
