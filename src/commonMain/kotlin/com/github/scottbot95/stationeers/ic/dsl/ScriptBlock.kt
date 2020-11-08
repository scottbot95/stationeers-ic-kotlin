package com.github.scottbot95.stationeers.ic.dsl

@DslMarker
annotation class ScriptBlockMarker

@ScriptBlockMarker
abstract class ScriptBlock : Compilable {
    abstract operator fun Compilable.unaryPlus()
}

open class SimpleScriptBlock : ScriptBlock() {
    private val operations = mutableListOf<Compilable>()

    override fun Compilable.unaryPlus() {
        operations.add(this)
    }

    override fun compile(options: CompileOptions): CompileResults = operations.asSequence()
        .map { it.compile(options) }
        .reduce { combined: CompileResults, it: CompileResults -> combined + it }
}

fun ScriptBlock.block(init: ScriptBlock.() -> Unit): ScriptBlock {
    val block = SimpleScriptBlock()
    block.init()
    +block
    return block
}