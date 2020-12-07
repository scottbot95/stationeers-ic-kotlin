package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.CompiledOperation
import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.util.AliasedScriptValueContainer
import com.github.scottbot95.stationeers.ic.util.DelegatingAliasedScriptValueContainer
import com.github.scottbot95.stationeers.ic.util.DelegatingLabelContainer
import com.github.scottbot95.stationeers.ic.util.LabelContainer
import com.github.scottbot95.stationeers.ic.util.LabelContainerImpl
import com.github.scottbot95.stationeers.ic.util.combine
import com.github.scottbot95.stationeers.ic.util.compileAll

@DslMarker
annotation class ScriptDSL

@ScriptDSL
interface ScriptBlock : Compilable {
    val registers: AliasedScriptValueContainer<Register>
    val devices: AliasedScriptValueContainer<Device>
    val labels: LabelContainer

    operator fun Compilable.unaryPlus()

    operator fun String.unaryPlus() {
        +Compilable { partial ->
            partial + CompiledOperation(ScriptValue.of(this)) {
                // TODO improve this warning
                println("WARN: Text added directly is not currently simulated.")
                println("\tIgnoring `$this`")
                it.next()
            }
        }
    }

    fun doFirst(init: ScriptBlock.() -> Unit) = doFirst(SimpleScriptBlock(this, 0).apply(init))
    fun doFirst(block: Compilable)

    fun doLast(init: ScriptBlock.() -> Unit) = doLast(SimpleScriptBlock(this, 0).apply(init))
    fun doLast(block: Compilable)

    /**
     * [LineReference] to the start of the [ScriptBlock]
     */
    val start: LineReference

    /**
     * [LineReference] to the end of the [ScriptBlock]
     */
    val end: LineReference
}

/**
 * Basic implementation of [ScriptBlock].
 *
 * Provides basic implementation of [compile] and ensures containers get set appropriate from the [scope].
 * Also ensure that [start] and [end] get injected correctly
 */
open class SimpleScriptBlock(val scope: ScriptBlock? = null, private val spacing: Int = 1) : ScriptBlock {
    private val startBlocks = mutableListOf<Compilable>()
    private val endBlocks = mutableListOf<Compilable>()

    private val operations = mutableListOf<Compilable>()

    override val labels = scope?.let { DelegatingLabelContainer(it.labels) } ?: LabelContainerImpl()

    override val start: LineReference = reference(inject = false).apply { doFirst(inject) }
    override val end: LineReference = reference(inject = false).apply { doLast(inject) }

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

    override fun compile(partial: PartialCompiledScript) = listOf(
        Spacer(spacing),
        startBlocks.combine(),
        operations.combine(),
        endBlocks.asReversed().combine(),
    ).compileAll(partial)
}
