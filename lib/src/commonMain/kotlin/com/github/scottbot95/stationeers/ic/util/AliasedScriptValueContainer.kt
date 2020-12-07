package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.Operation
import com.github.scottbot95.stationeers.ic.dsl.AliasedScriptValue
import com.github.scottbot95.stationeers.ic.dsl.Compilable
import com.github.scottbot95.stationeers.ic.dsl.PartialCompiledScript
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue
import com.github.scottbot95.stationeers.ic.dsl.SimpleAliasedScriptValue

typealias AliasedScriptValueConstructor<T, U> = (alias: String?, value: ScriptValue<T>, release: () -> Unit) -> U

abstract class AliasedScriptValueContainer<T : Any>(private val prefix: String = "") : Compilable {

    private val valuesInUse = CountingSet<T>()

    private val aliasesUsed = mutableMapOf<String, T>()

    val aliases: Map<String, T> get() = aliasesUsed.toMap()

    /**
     * Returns an aliased reference for the given [desiredValue] if provided or the next unused [T] if given null.
     * You may optionally specify an alias for this [T] with [name].
     *
     * You may have multiple [AliasedScriptValue]s pointing the the same [T], however [newAliasedValue] will
     * throw if there are no unused registers and [desiredValue] is not provided.
     */
    fun <V : AliasedScriptValue<T>> newAliasedValue(
        desiredValue: T?,
        name: String?,
        createAliasedValue: AliasedScriptValueConstructor<T, V>
    ): V {
        val valueToUse = desiredValue
            ?: nextFreeValue()
            ?: throw IllegalArgumentException("Must provided a desiredValue when all values are in use")

        val prefixedName = name?.let { prefix + it }

        if (prefixedName !== null) {
            if (prefixedName in aliasesUsed) {
                throw IllegalStateException("Rebinding aliases is not currently supported")
            }
            aliasesUsed[prefixedName] = valueToUse
        }

        valuesInUse.addOne(valueToUse)

        return createAliasedValue(prefixedName, newInstance(valueToUse)) {
            valuesInUse.removeOne(valueToUse)
        }
    }

    fun newAliasedValue(desiredValue: T?, name: String?): AliasedScriptValue<T> =
        newAliasedValue(
            desiredValue,
            name
        ) { alias: String?, value: ScriptValue<T>, release: () -> Unit ->
            SimpleAliasedScriptValue(
                alias,
                value,
                release
            )
        }

    fun getUsed(value: T): Int = valuesInUse[value]

    override fun compile(partial: PartialCompiledScript): PartialCompiledScript {
        return if (partial.options.minify) {
            partial
        } else {
            aliases
                .map { Operation.Alias(it.key, it.value) }
                .compileAll(partial)
        }
    }

    abstract fun nextFreeValue(): T?

    abstract fun newInstance(value: T): ScriptValue<T>
}

class DelegatingAliasedScriptValueContainer<T : Any>(
    private val delegate: AliasedScriptValueContainer<T>
) : AliasedScriptValueContainer<T>() {
    override fun nextFreeValue() = delegate.nextFreeValue()

    override fun newInstance(value: T) = delegate.newInstance(value)
}
