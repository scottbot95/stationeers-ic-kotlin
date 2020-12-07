package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.simulation.SimulationState
import com.github.scottbot95.stationeers.ic.util.once
import io.ktor.utils.io.core.Closeable

// TODO We should probably make this a sealed class with only the allowed types
interface ScriptValue<out T : Any> : Closeable {
    val value: T

    fun toString(context: CompileContext): String {
        return when (val value = this.value) {
            is ScriptValue<*> -> toString(context)
            is Double -> {
                if (value.toInt().toDouble() == value) {
                    value.toInt().toString()
                } else {
                    value.toString()
                }
            }
            else -> value.toString()
        }
    }

    override fun close() = Unit

    companion object {
        val EMPTY = ScriptValue.of("")
    }
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
fun ScriptValue.Companion.of(value: Double): ScriptValue<Double> = SimpleScriptValue(value)

@ExperimentalUnsignedTypes
fun ScriptValue.Companion.of(value: UInt): ScriptValue<UInt> = SimpleScriptValue(value)
fun ScriptValue.Companion.of(value: Device): ScriptValue<Device> = SimpleScriptValue(value)
fun ScriptValue.Companion.of(value: Register): ScriptValue<Register> = SimpleScriptValue(value)
fun ScriptValue.Companion.of(parts: List<ScriptValue<*>>): ScriptValue<String> = CombinedScriptValue(parts)
fun ScriptValue.Companion.of(vararg parts: ScriptValue<*>): ScriptValue<String> = CombinedScriptValue(parts.toList())

fun ScriptValue<*>.toDouble(state: SimulationState) = value.let {
    when (it) {
        is Number -> it.toDouble()
        is Register -> state.registers.getValue(it)
        else -> throw IllegalArgumentException("Operand can must be a Number or Register. Given ${this::class.simpleName}")
    }
}
