package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.Operation
import com.github.scottbot95.stationeers.ic.dsl.AliasedScriptValue
import com.github.scottbot95.stationeers.ic.dsl.Compilable
import com.github.scottbot95.stationeers.ic.dsl.CompileContext
import com.github.scottbot95.stationeers.ic.dsl.CompileOptions
import com.github.scottbot95.stationeers.ic.dsl.CompileResults
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue
import com.github.scottbot95.stationeers.ic.dsl.plus

abstract class AliasedScriptValueContainer<T : Any> : Compilable {

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
    fun newAliasedValue(desiredValue: T?, name: String?): AliasedScriptValue<T> {
        val valueToUse = desiredValue
            ?: nextFreeValue()
            ?: throw IllegalArgumentException("Must provided a desiredValue when all values are in use")

        if (name !== null) {
            if (name in aliasesUsed) {
                throw IllegalStateException("Rebinding aliases is not currently supported")
            }
            aliasesUsed[name] = valueToUse
        }

        valuesInUse.addOne(valueToUse)

        return AliasedScriptValue(
            name,
            newInstance(valueToUse)
        ) {
            valuesInUse.removeOne(valueToUse)
        }
    }

    fun getUsed(value: T): Int = valuesInUse[value]

    override fun compile(options: CompileOptions, context: CompileContext): CompileResults = aliases
        .map { Operation.Alias(it.key, it.value).compile(options, context) }
        .reduceOrNull() { acc, it -> acc + it } ?: CompileResults()

    abstract fun nextFreeValue(): T?

    abstract fun newInstance(value: T): ScriptValue<T>
}

class DelegatingAliasedScriptValueContainer<T : Any>(
    private val delegate: AliasedScriptValueContainer<T>
) : AliasedScriptValueContainer<T>() {
    override fun nextFreeValue(): T? = delegate.nextFreeValue()

    override fun newInstance(value: T): ScriptValue<T> = delegate.newInstance(value)
}
