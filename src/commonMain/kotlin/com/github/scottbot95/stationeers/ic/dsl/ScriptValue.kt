package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.Register
import io.ktor.utils.io.core.Closeable

interface ScriptValue<out T : Any> : Closeable {
    val value: T

    fun toString(options: CompileOptions): String = value.toString()

    override fun close() = Unit

    companion object
}

// TODO make this sealed?
class SimpleScriptValue<T : Any>(override val value: T) : ScriptValue<T>

class AliasedScriptValue<T : Any>(
    val alias: String?,
    private val delegate: ScriptValue<T>,
    private val releaseAlias: () -> Unit = {},
) : ScriptValue<T> by delegate {
    override fun toString(options: CompileOptions): String =
        if (options.minify || alias === null) delegate.toString(options) else alias

    override fun close() = releaseAlias()
}

// TODO Make this its own file?
fun ScriptValue.Companion.of(value: String): ScriptValue<String> = SimpleScriptValue(value)
fun ScriptValue.Companion.of(value: Number): ScriptValue<Number> = SimpleScriptValue(value)
fun ScriptValue.Companion.of(value: Device): ScriptValue<Device> = SimpleScriptValue(value)
fun ScriptValue.Companion.of(value: Register): ScriptValue<Register> = SimpleScriptValue(value)
