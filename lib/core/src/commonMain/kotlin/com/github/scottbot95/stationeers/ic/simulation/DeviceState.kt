package com.github.scottbot95.stationeers.ic.simulation

interface DeviceState {
    val connected: Boolean

    fun getField(field: String): Double
    fun setField(field: String, value: Double)
    // TODO add reagents and slots here too
}
