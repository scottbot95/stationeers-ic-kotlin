package com.github.scottbot95.stationeers.ic.dsl

@DslMarker
annotation class ScriptBlockMarker

@ScriptBlockMarker
interface ScriptBlock : Compilable {
    operator fun Compilable.unaryPlus()

    val registers: RegisterContainer
}

abstract class AbstractScriptBlock(val scope: ScriptBlock? = null) : ScriptBlock {
    override val registers = RegisterContainer()
}

open class SimpleScriptBlock(scope: ScriptBlock? = null) : AbstractScriptBlock(scope) {
    private val operations = mutableListOf<Compilable>()

    override fun Compilable.unaryPlus() {
        operations.add(this)
    }

    override fun compile(options: CompileOptions): CompileResults = operations.asSequence()
        .map { it.compile(options) }
        .reduce { combined: CompileResults, it: CompileResults -> combined + it }
}

fun ScriptBlock.block(init: ScriptBlock.() -> Unit): ScriptBlock {
    val block = SimpleScriptBlock(this)
    block.init()
    +block
    return block
}

fun ScriptBlock.registers(init: RegisterContainer.() -> Unit): RegisterContainer {
    registers.init()
    return registers
}