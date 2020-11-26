package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.util.AliasedScriptValueContainer
import com.github.scottbot95.stationeers.ic.util.DelegatingAliasedScriptValueContainer
import com.github.scottbot95.stationeers.ic.util.compileAll

@DslMarker
annotation class ScriptBlockMarker

@ScriptBlockMarker
interface ScriptBlock : Compilable {
    operator fun Compilable.unaryPlus()

    operator fun String.unaryPlus() {
        +Compilable { context -> CompileResults(context, this) }
    }

    val registers: AliasedScriptValueContainer<Register>
    val devices: AliasedScriptValueContainer<Device>
}

abstract class AbstractScriptBlock(val scope: ScriptBlock? = null) : ScriptBlock {
    override val registers = scope?.let { DelegatingAliasedScriptValueContainer(it.registers) } ?: RegisterContainer()
    override val devices = scope?.let { DelegatingAliasedScriptValueContainer(it.devices) } ?: DeviceContainer()
}

open class SimpleScriptBlock(scope: ScriptBlock? = null) : AbstractScriptBlock(scope) {
    private val operations = mutableListOf<Compilable>()

    override fun Compilable.unaryPlus() {
        operations.add(this)
    }

    override fun compile(context: CompileContext): CompileResults {
        val aliasResults = if (context.compileOptions.minify) {
            listOf()
        } else {
            listOf(devices, registers)
        }.compileAll(context)

        // TODO do the wait for devices thing here

        val operationsResults = operations.compileAll(aliasResults.endContext)

        return aliasResults + operationsResults
    }
}
