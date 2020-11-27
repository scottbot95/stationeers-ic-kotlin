package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.util.once
import io.ktor.utils.io.core.Closeable

interface ScriptValue<out T : Any> : Closeable {
    val value: T

    fun toString(context: CompileContext): String = when (value) {
        is ScriptValue<*> -> toString(context)
        else -> value.toString()
    }

    override fun close() = Unit

    companion object
}

data class SimpleScriptValue<out T : Any>(override val value: T) : ScriptValue<T>

interface AliasedScriptValue<out T : Any> : ScriptValue<T> {
    val alias: String?
}

// TODO I do not like passing a lambda to release alias.
//  Maybe store the container and add a public method to release aliases? Probably want to track instances again in that case
open class SimpleAliasedScriptValue<out T : Any>(
    override val alias: String?,
    private val delegate: ScriptValue<T>,
    releaseAlias: () -> Unit = {},
) : ScriptValue<T> by delegate, AliasedScriptValue<T> {
    private val releaseOnce = once(releaseAlias)

    override fun toString(context: CompileContext): String {
        // Need this for null check to work correctly
        val theAlias = alias
        return if (context.compileOptions.minify || theAlias == null) delegate.toString(context) else theAlias
    }

    override fun close() = releaseOnce()
}

data class CombinedScriptValue(private val parts: List<ScriptValue<*>>) : ScriptValue<String> {
    constructor(vararg parts: ScriptValue<*>) : this(parts.toList())

    override val value: String by lazy { parts.joinToString(" ") { it.value.toString() } }

    override fun toString(context: CompileContext): String = parts.joinToString(" ") { it.toString(context) }
}

// TODO Make this its own file?
fun ScriptValue.Companion.of(value: String): ScriptValue<String> = SimpleScriptValue(value)
fun ScriptValue.Companion.of(value: Number): ScriptValue<Number> = SimpleScriptValue(value)
fun ScriptValue.Companion.of(value: Device): ScriptValue<Device> = SimpleScriptValue(value)
fun ScriptValue.Companion.of(value: Register): ScriptValue<Register> = SimpleScriptValue(value)
fun ScriptValue.Companion.of(parts: List<ScriptValue<*>>): ScriptValue<String> = CombinedScriptValue(parts)
