package com.github.scottbot95.stationeers.ic.ir

import kotlin.reflect.KMutableProperty0

data class IRCompileContext(
    var regCount: UInt = 0U,
    val variables: MutableMap<String, IRRegister> = mutableMapOf(),
    val globals: Map<String, IRRegister> = emptyMap(),
    /**
     * Reference to set the next statement in the "default" flow case (continuing chain of && and || and going inside a loop)
     */
    var next: KMutableProperty0<IRStatement?>,
)

fun IRCompileContext.makeReg(): IRRegister = IRRegister(regCount++)

inline fun IRCompileContext.withReg(block: (IRRegister) -> Unit): IRRegister = makeReg().also(block)

operator fun IRCompileContext.plusAssign(statement: IRStatement) {
    next.set(statement)
    next = statement::next
}
