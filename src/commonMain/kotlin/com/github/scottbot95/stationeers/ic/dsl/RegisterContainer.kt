package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.NamedRegisterValue
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.RegisterValue
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class RegisterValueProvider(
    private val container: RegisterContainer,
    private val register: Register? = null,
    private val name: String? = null
) {
    operator fun provideDelegate(
        thisRef: Any?,
        prop: KProperty<*>
    ) = ReadOnlyProperty<Any?, RegisterValue> { _, _ ->
        container.newRegister(register, name ?: prop.name)
    }
}

inline val RegisterContainer.register: RegisterValueProvider
    get() = this.register()

fun RegisterContainer.register(register: Register? = null, name: String? = null): RegisterValueProvider =
    RegisterValueProvider(this, register, name)

@ScriptBlockMarker
interface RegisterContainer {
    /**
     * Returns a reference for the given [register] if provided or the next unused register if given null.
     * You may optionally specify an alias for this register with [name]
     *
     * You may have multiple [RegisterValue]s pointing the the same [Register], however [RegisterContainer.newRegister] will
     * throw if there are no unused registers and [register] is not provided
     */
    fun newRegister(register: Register? = null, name: String? = null): RegisterValue

}

class RegisterContainerImpl : RegisterContainer {
    private val registersInUse: Map<Register, MutableSet<RegisterValue>> = Register.values()
        .map { it to mutableSetOf<RegisterValue>() }
        .toMap()

    override fun newRegister(register: Register?, name: String?): RegisterValue {
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
