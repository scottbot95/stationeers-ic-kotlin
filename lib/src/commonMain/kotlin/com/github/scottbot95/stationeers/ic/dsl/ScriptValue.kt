package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.util.once
import io.ktor.utils.io.core.Closeable

interface ScriptValue<out T : Any> : Closeable {
    val value: T

    fun toString(options: CompileOptions): String = when (value) {
        is ScriptValue<*> -> toString(options)
        else -> value.toString()
    }

    override fun close() = Unit

    companion object
}

class SimpleScriptValue<out T : Any>(override val value: T) : ScriptValue<T>

// TODO I do not like passing a lambda to release alias.
//  Maybe store the container and add a public method to release aliases? Probably want to track instances again in that case
open class AliasedScriptValue<out T : Any>(
    val alias: String?,
    private val delegate: ScriptValue<T>,
    releaseAlias: () -> Unit = {},
) : ScriptValue<T> by delegate {
    private val releaseOnce = once(releaseAlias)

    override fun toString(options: CompileOptions): String =
        if (options.minify || alias === null) delegate.toString(options) else alias

    override fun close() = releaseOnce()
}

// TODO Make this its own file?
fun ScriptValue.Companion.of(value: String): ScriptValue<String> = SimpleScriptValue(value)
fun ScriptValue.Companion.of(value: Number): ScriptValue<Number> = SimpleScriptValue(value)
fun ScriptValue.Companion.of(value: Device): ScriptValue<Device> = SimpleScriptValue(value)
fun ScriptValue.Companion.of(value: Register): ScriptValue<Register> = SimpleScriptValue(value)
