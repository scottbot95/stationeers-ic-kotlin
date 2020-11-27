package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.util.AliasedScriptValueContainer
import com.github.scottbot95.stationeers.ic.util.DelegatingAliasedScriptValueContainer
import com.github.scottbot95.stationeers.ic.util.combine
import com.github.scottbot95.stationeers.ic.util.compileAll

@DslMarker
annotation class ScriptBlockMarker

@ScriptBlockMarker
interface ScriptBlock : Compilable {
    val registers: AliasedScriptValueContainer<Register>
    val devices: AliasedScriptValueContainer<Device>

    operator fun Compilable.unaryPlus()

    operator fun String.unaryPlus() {
        +Compilable { context -> CompileResults(context, CompiledLine(this)) }
    }

    fun doFirst(init: ScriptBlock.() -> Unit)
    fun doLast(init: ScriptBlock.() -> Unit)
}

abstract class AbstractScriptBlock(val scope: ScriptBlock? = null) : ScriptBlock {
    override val registers = scope?.let { DelegatingAliasedScriptValueContainer(it.registers) } ?: RegisterContainer()
    override val devices = scope?.let { DelegatingAliasedScriptValueContainer(it.devices) } ?: DeviceContainer()
}

open class SimpleScriptBlock(scope: ScriptBlock? = null) : AbstractScriptBlock(scope) {
    private val startBlockDelegate = lazy { SimpleScriptBlock(this) }
    private val startBlock by startBlockDelegate

    private val endBlockDelegate = lazy { SimpleScriptBlock(this) }
    private val endBlock by endBlockDelegate

    private val operations = mutableListOf<Compilable>()

    override fun Compilable.unaryPlus() {
        operations.add(this)
    }

    override fun doFirst(init: ScriptBlock.() -> Unit) = startBlock.run(init)

    override fun doLast(init: ScriptBlock.() -> Unit) = endBlock.run(init)

    override fun compile(context: CompileContext): CompileResults {
        val aliasBlock = if (context.compileOptions.minify) {
            listOf()
        } else {
            listOf(devices, registers)
        }

        return listOfNotNull(
            aliasBlock.combine(),
            if (startBlockDelegate.isInitialized()) startBlock else null,
            operations.combine(),
            if (endBlockDelegate.isInitialized()) endBlock else null,
        ).compileAll(context)
    }
}
