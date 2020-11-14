package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.dsl.AliasedScriptValue
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue

abstract class AliasedScriptValueContainer<T : Any> {

    private val valuesInUse = CountingSet<T>()

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

        valuesInUse.add(valueToUse)

        return AliasedScriptValue(
            name,
            newInstance(valueToUse),
            once {
                valuesInUse.remove(valueToUse)
            }
        )
    }

    fun getUsed(value: T): Int = valuesInUse[value]

    protected abstract fun nextFreeValue(): T?

    protected abstract fun newInstance(value: T): ScriptValue<T>
}
