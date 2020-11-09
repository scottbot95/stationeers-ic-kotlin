package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.NamedRegisterValue
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.RegisterValue

@ScriptBlockMarker
class RegisterContainer {

    private val registersInUse: Map<Register, MutableSet<RegisterValue>> = Register.values()
        .map { it to mutableSetOf<RegisterValue>() }
        .toMap()

    /**
     * Returns a reference for the given [register] if provided or the next unused register if given null.
     * You may optionally specify an alias for this register with [name]
     *
     * You may have multiple [RegisterValue]s pointing the the same [Register], however [RegisterContainer.register] will
     * throw if there are no unused registers and [register] is not provided
     */
    fun register(register: Register? = null, name: String? = null): RegisterValue {
        val registerToUse = register ?: nextFreeRegister()
        if (registerToUse === null) {
            throw IllegalArgumentException("All registers in use and no register was explicitly provided")
        }

        return NamedRegisterValue(registerToUse, name) {
            registersInUse[registerToUse]?.remove(this)
        }
    }

    private fun nextFreeRegister(): Register? {
        // TODO This could be optimized by caching the earliest known unused register
        return registersInUse.entries.firstOrNull { it.value.size == 0 }?.key
    }
}
