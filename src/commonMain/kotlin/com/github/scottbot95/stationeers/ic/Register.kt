package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.dsl.CompileOptions
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue
import com.github.scottbot95.stationeers.ic.util.once
import io.ktor.utils.io.core.Closeable

enum class Register {
    R0, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15;

    override fun toString(): String = name.toLowerCase()
}

class RegisterValue(
    val register: Register,
    release: RegisterValue.() -> Unit
) : ScriptValue, Closeable {
    private val releaseOnce = once { this.release() }

    override fun toString(options: CompileOptions): String = register.toString()

    override fun close() = releaseOnce()
}
