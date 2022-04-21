package com.github.scottbot95.stationeers.ic.ir

sealed class IRStatement(val opCode: String, val params: List<IRRegister>) {
    constructor(opCode: String, vararg params: IRRegister) : this(opCode, params.toList())

    var next: IRStatement? = null
    var cond: IRStatement? = null

//    abstract val next: IRStatement?

    override fun toString(): String = params.joinToString(" ", prefix = "$opCode ").trimEnd()

    class Nop : IRStatement("nop")
    data class Init(
        val reg: IRRegister,
        val ident: String?,
        val value: Number, // TODO should IR maintain types? probably....

    ) : IRStatement("init", reg) {
        override fun toString(): String = "$opCode $reg $ident $value"
    }

    data class Add(
        val dest: IRRegister,
        val a: IRRegister,
        val b: IRRegister,
    ) : IRStatement("add", dest, a, b)

    data class Negate(
        val dest: IRRegister,
        val src: IRRegister,
    ) : IRStatement("neg", dest, src)

    data class Copy(
        val dest: IRRegister,
        val src: IRRegister,
    ) : IRStatement("copy", dest, src)

    data class Equals(
        val dest: IRRegister,
        val a: IRRegister,
        val b: IRRegister,
    ) : IRStatement("eq", dest, a, b)

    data class IfNotZero(
        val check: IRRegister,
        val label: String? = null,
    ) : IRStatement("ifnz", check) {
        override fun toString(): String = "$opCode $check $label"
    }

    data class FunctionCall(
        val result: IRRegister,
        val func: IRRegister,
        val parameters: List<IRRegister>,
    ) : IRStatement("fcall", listOf(result, func) + parameters) {
        constructor(
            result: IRRegister,
            func: IRRegister,
            vararg parameters: IRRegister,
        ) : this(result, func, parameters.toList())
    }

    data class Return(val result: IRRegister) : IRStatement("ret", result)
}
