package com.github.scottbot95.stationeers.ic.simulation

interface RegisterStates {
    operator fun get(register: Register): Double
}

interface DeviceStates {
    operator fun get(deviceId: Int): DeviceState
}

class ICStack(val values: DoubleArray) {

    init {
        require(values.size == 512)
    }

    operator fun get(i: Int): Double = values[i]

    operator fun set(i: Int, value: Double) { values[i] = value }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (!this::class.isInstance(other)) return false
        other as ICStack
        return values.contentEquals(other.values)
    }

    override fun hashCode(): Int {
        return values.contentHashCode()
    }

    override fun toString(): String = "Stack: [ ${values.joinToString()} ]"
}

data class SimulationState(
    val registers: RegisterStates,
    val devices: DeviceStates,
    val stack: ICStack,
)
