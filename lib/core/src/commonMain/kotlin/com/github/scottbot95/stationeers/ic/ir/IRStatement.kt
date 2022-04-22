package com.github.scottbot95.stationeers.ic.ir

sealed class IRStatement(val opCode: String, val params: List<IRRegister>) {
    constructor(opCode: String, vararg params: IRRegister) : this(opCode, params.toList())

    sealed class ConditionalStatement(
        opCode: String,
        val check: IRRegister,
        var jumpLabel: String? = null, // TODO should this really be a var???
        vararg params: IRRegister
    ) : IRStatement(opCode, check, *params) {
        /**
         * Reference to the next statement when a branch statement is true
         */
        var cond: IRStatement? = null

        override fun toString(): String = "$opCode $check $jumpLabel"
    }

    /**
     * Reference to the next statement after this one in the "false" case of branches (default)
     */
    var next: IRStatement? = null

    private val prev: MutableList<IRStatement> = mutableListOf()

//    abstract val next: IRStatement?

    override fun toString(): String = params.joinToString(" ", prefix = "$opCode ").trimEnd()

    class Nop : IRStatement("nop")
    class Init(
        val reg: IRRegister,
        val ident: String?,
        val value: Number, // TODO should IR maintain types? probably....

    ) : IRStatement("init", reg) {
        override fun toString(): String = "$opCode $reg $ident $value"
    }

    class Add(
        val dest: IRRegister,
        val a: IRRegister,
        val b: IRRegister,
    ) : IRStatement("add", dest, a, b)

    class Negate(
        val dest: IRRegister,
        val src: IRRegister,
    ) : IRStatement("neg", dest, src)

    class Copy(
        val dest: IRRegister,
        val src: IRRegister,
    ) : IRStatement("copy", dest, src)

    class Equals(
        val dest: IRRegister,
        val a: IRRegister,
        val b: IRRegister,
    ) : IRStatement("eq", dest, a, b)

    class IfNotZero(
        check: IRRegister,
        label: String? = null,
    ) : ConditionalStatement("ifnz", check, label)

    class IfZero(
        check: IRRegister,
        label: String? = null,
    ) : ConditionalStatement("ifz", check, label)

    class FunctionCall(
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

    class Return(val result: IRRegister) : IRStatement("ret", result)

    class Jump(val label: String) : IRStatement("jmp $label")

    class Label(val label: String) : IRStatement("$label:")

    class Halt : IRStatement("hcf")
}

inline val IRStatement.cond: IRStatement? get() = (this as? IRStatement.ConditionalStatement)?.cond

/**
 * Only guaranteed to work on un-optimized code as all branches are guaranteed to merge back to a nop at some point
 */
val IRStatement.end: IRStatement get() = next?.end ?: this
