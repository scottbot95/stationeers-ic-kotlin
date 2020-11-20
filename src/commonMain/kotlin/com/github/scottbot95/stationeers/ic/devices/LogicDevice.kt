package com.github.scottbot95.stationeers.ic.devices

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue
import com.github.scottbot95.stationeers.ic.util.Gases

typealias LogicVarMap = Map<String, Boolean>

private fun <K, V> mergeMaps(vararg maps: Map<K, V>): Map<K, V> = maps.map { it.asSequence() }
    .reduce { acc, it -> acc + it }
    .distinct()
    .groupBy({ it.key }, { it.value })
    .mapValues { (_, it) -> it.last() }

val STRUCTURE_LOGIC_VARS: LogicVarMap = mapOf("PrefabHash" to false)
val POWERED_LOGIC_VARS: LogicVarMap = mapOf("On" to true, "Powered" to false, "RequiredPower" to false)
val SETTABLE_LOGIC_VARS: LogicVarMap = mapOf("Setting" to true)
val LOCKABLE_LOGIC_VARS: LogicVarMap = mapOf("Lock" to true)
val GAS_READER_LOGIC_VARS: LogicVarMap = (Gases.values().map { it.name } + listOf("")).map { it to false }.toMap()


/**
 * Generic interface representing available device variables for a LogicDevice
 * TODO May be used to facilitate simulation in the future
 */
interface LogicDevice {
    fun canRead(value: String): Boolean = false
    fun canWrite(value: String): Boolean = false
}

abstract class DefaultLogicDevice(private val deviceVarMap: LogicVarMap) : LogicDevice {
    constructor(vararg varMaps: LogicVarMap) : this(mergeMaps(*varMaps))

    override fun canRead(value: String): Boolean = deviceVarMap.containsKey(value)
    override fun canWrite(value: String): Boolean = deviceVarMap[value] ?: false
}

abstract class StructureLogicDevice(vararg deviceVarMap: LogicVarMap) :
    DefaultLogicDevice(STRUCTURE_LOGIC_VARS, *deviceVarMap)

object Light : StructureLogicDevice(POWERED_LOGIC_VARS, LOCKABLE_LOGIC_VARS)
object LogicMemory : StructureLogicDevice(SETTABLE_LOGIC_VARS)
object GasSensor : StructureLogicDevice(GAS_READER_LOGIC_VARS)



// THIS GOES INTO ANOTHER FILE!!!


class LogicDeviceScriptValue<T: LogicDevice>(val logicDevice: T, override val value: Device): ScriptValue<Device>

private fun <T : LogicDevice> writeDevice(device: LogicDeviceScriptValue<T>, deviceVar: String) {
    if (!device.logicDevice.canWrite(deviceVar)) {
        throw IllegalArgumentException("Cannot write to $deviceVar on a ${device.logicDevice::class.simpleName}")
    }
}
