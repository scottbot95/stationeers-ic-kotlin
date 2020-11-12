package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.RegisterValue

inline val RegisterContainer.register
    get() = this.register()

fun RegisterContainer.register(register: Register? = null, name: String? = null) =
    AliasedScriptValueDelegateProvider(name) {
        this.newRegister(register, it)
    }

@ScriptBlockMarker
interface RegisterContainer {
    /**
     * Returns a reference for the given [register] if provided or the next unused register if given null.
     * You may optionally specify an alias for this register with [name]
     *
     * You may have multiple [RegisterValue]s pointing the the same [Register], however [RegisterContainer.newRegister] will
     * throw if there are no unused registers and [register] is not provided
     */
    fun newRegister(register: Register? = null, name: String? = null): AliasedScriptValue<RegisterValue>
}

class RegisterContainerImpl : RegisterContainer {
    private val registersInUse: Map<Register, MutableSet<AliasedScriptValue<RegisterValue>>> = Register.values()
        .map { it to mutableSetOf<AliasedScriptValue<RegisterValue>>() }
        .toMap()

    override fun newRegister(register: Register?, name: String?): AliasedScriptValue<RegisterValue> {
        val registerToUse = register ?: nextFreeRegister()
        if (registerToUse === null) {
            throw IllegalArgumentException("All registers in use and no register was explicitly provided")
        }

        val aliasSet = registersInUse[registerToUse]!!
        val registerValue = AliasedScriptValue(
            name,
            RegisterValue(registerToUse) {
                aliasSet.remove<ScriptValue>(this)
            }
        )

        aliasSet.add(registerValue)

        return registerValue
    }

    private fun nextFreeRegister(): Register? {
        // TODO This could be optimized by caching the earliest known unused register
        return registersInUse.entries.firstOrNull { it.value.size == 0 }?.key
    }
}
