package com.github.scottbot95.stationeers.ic.dsl

@DslMarker
annotation class ScriptBlockMarker

@ScriptBlockMarker
interface ScriptBlock : Compilable {
    operator fun Compilable.unaryPlus()

    operator fun String.unaryPlus() {
        +Compilable { _, _ -> CompileResults(this) }
    }

    val registers: RegisterContainer
    val devices: DeviceContainer
}

abstract class AbstractScriptBlock(val scope: ScriptBlock? = null) : ScriptBlock {
    override val registers = RegisterContainer()
    override val devices = DeviceContainer()
}

open class SimpleScriptBlock(scope: ScriptBlock? = null) : AbstractScriptBlock(scope) {
    private val operations = mutableListOf<Compilable>()

    override fun Compilable.unaryPlus() {
        operations.add(this)
    }

    override fun compile(options: CompileOptions, context: CompileContext): CompileResults =
        operations.fold(CompileResults() to context) { (acc, currContext), it ->
            it.compile(options, currContext).let {
                (acc + it) to currContext + it.lines.size
            }
        }.first
}
