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

    fun doFirst(init: ScriptBlock.() -> Unit) = doFirst(SimpleScriptBlock(this, 0).apply(init))
    fun doFirst(block: Compilable)

    fun doLast(init: ScriptBlock.() -> Unit) = doLast(SimpleScriptBlock(this, 0).apply(init))
    fun doLast(block: Compilable)

    val start: LineReference
    val end: LineReference
}

open class SimpleScriptBlock(val scope: ScriptBlock? = null, private val spacing: Int = 1) : ScriptBlock {
    private val startBlocks = mutableListOf<Compilable>()
    private val endBlocks = mutableListOf<Compilable>()

    private val operations = mutableListOf<Compilable>()

    override val start = FixedLineReference().apply { doFirst(inject) }
    override val end = FixedLineReference().apply { doLast(inject) }

    override val devices =
        scope?.let { DelegatingAliasedScriptValueContainer(it.devices) }
            ?: DeviceContainer().apply(::doFirst)

    override val registers =
        scope?.let { DelegatingAliasedScriptValueContainer(it.registers) }
            ?: RegisterContainer().also(::doFirst)

    override fun Compilable.unaryPlus() {
        operations.add(this)
    }

    override fun doFirst(block: Compilable) {
        startBlocks.add(block)
    }

    override fun doLast(block: Compilable) {
        endBlocks.add(block)
    }

    override fun compile(context: CompileContext): CompileResults = listOfNotNull(
        startBlocks.combine(),
        operations.combine(),
        endBlocks.asReversed().combine(),
        Spacer(spacing),
    ).compileAll(context)
}
