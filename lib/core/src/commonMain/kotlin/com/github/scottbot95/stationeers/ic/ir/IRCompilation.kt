package com.github.scottbot95.stationeers.ic.ir

data class IRFunction(
    val name: String,
    val entrypoint: IRStatement,
    val numParams: Int,
)

data class IRCompileContext(
    var regCount: UInt = 0U,
    val variables: MutableMap<Int, IRRegister> = mutableMapOf(),
//    val statements: MutableIRStatementList = LinkedIRStatementList(),
    var lastStatement: IRStatement? = null,
)

fun IRCompileContext.makeReg(): IRRegister = IRRegister(regCount++)

inline fun IRCompileContext.withReg(block: (IRRegister) -> Unit): IRRegister = makeReg().also(block)

data class IRCompilation(
    val functions: Map<String, IRFunction>
)