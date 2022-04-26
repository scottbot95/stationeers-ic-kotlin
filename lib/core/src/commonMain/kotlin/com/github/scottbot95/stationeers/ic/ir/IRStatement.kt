package com.github.scottbot95.stationeers.ic.ir

import com.github.scottbot95.stationeers.ic.ir.IRStatement.ConditionalStatement

sealed class IRStatement(val opCode: String, val params: List<IRRegister>) {
    constructor(opCode: String, vararg params: IRRegister) : this(opCode, params.toList())

    sealed class WritingStatement(opCode: String, val dest: IRRegister, vararg params: IRRegister) :
        IRStatement(opCode, dest, *params)

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
            set(value) {
                updateReference(value, field)
                field = value
            }

        override fun toString(): String = "$opCode $check, JMP $jumpLabel"

        /**
         * @return Whether the constant [value] matches this [ConditionalStatement]
         */
        abstract fun matches(value: Number): Boolean
    }

    /**
     * Reference to the next statement after this one in the "false" case of branches (default)
     */
    open var next: IRStatement? = null
        set(value) {
            updateReference(value, field)
            field = value
        }

    internal val prev: MutableList<IRStatement> = mutableListOf()

    override fun toString(): String = params.joinToString(" ", prefix = "$opCode ").trimEnd()

    class Nop : IRStatement("nop")
    class Init(
        dest: IRRegister,
        val ident: String?,
        val value: Number, // TODO should IR maintain types? probably....

    ) : WritingStatement("init", dest) {
        override fun toString(): String = "$opCode $dest $ident $value"
    }

    class Add(
        dest: IRRegister,
        val a: IRRegister,
        val b: IRRegister,
    ) : WritingStatement("add", dest, a, b)

    class Negate(
        dest: IRRegister,
        val src: IRRegister,
    ) : WritingStatement("neg", dest, src)

    class Copy(
        dest: IRRegister,
        val src: IRRegister,
    ) : WritingStatement("copy", dest, src)

    class Equals(
        dest: IRRegister,
        val a: IRRegister,
        val b: IRRegister,
    ) : WritingStatement("eq", dest, a, b)

    class IfNotZero(
        check: IRRegister,
        label: String? = null,
    ) : ConditionalStatement("ifnz", check, label) {
        override fun matches(value: Number): Boolean = value.toFloat() != 0f
    }

    class IfZero(
        check: IRRegister,
        label: String? = null,
    ) : ConditionalStatement("ifz", check, label) {
        override fun matches(value: Number): Boolean = value.toFloat() == 0f
    }

    class FunctionCall(
        dest: IRRegister,
        val func: IRRegister,
        val parameters: List<IRRegister>,
    ) : WritingStatement("fcall", dest, func, *parameters.toTypedArray()) {
        constructor(
            result: IRRegister,
            func: IRRegister,
            vararg parameters: IRRegister,
        ) : this(result, func, parameters.toList())
    }

    class Return(val result: IRRegister) : IRStatement("ret", result)

    class Halt : IRStatement("hcf")

    // The following are "fake" statements that are not really included in statement graph
    class Jump(val label: String) : IRStatement("jmp $label")
    class Label(val label: String) : IRStatement("$label:")
    class Placeholder : IRStatement("PLACEHOLDER - IF YOU SEE ME THERE ARE PROBLEMS") {
        // override to prevent updating prev reference in next statement
        override var next: IRStatement? = null
    }
}

private fun IRStatement.updateReference(
    new: IRStatement?,
    old: IRStatement?,
) {
    // This used to point to something, but now it doesn't. Delete the old prev reference
    old?.let {
        it.prev -= this
    }
    if (new != null) {
        // Add this to next statements prev references
        new.prev += this
    }
}

inline var IRStatement.cond: IRStatement?
    get() = (this as? ConditionalStatement)?.cond
    set(value) {
        (this as? ConditionalStatement)?.cond = value
    }

/**
 * Only guaranteed to work on un-optimized code as all branches are guaranteed to merge back to a nop at some point
 */
val IRStatement.end: IRStatement get() = next?.end ?: this

/**
 * @see [followChain]
 */
fun IRStatement.followNext(dedupe: Boolean = true): Iterable<IRStatement> = followChain(dedupe, followCond = false)

/**
 * Create an [Iterator] that marches through the [IRStatement.next] chain of this [IRStatement]
 *
 * @param dedupe Whether to quit when duplicates are shown. Can produce an infinite sequence if set to `false`
 */
fun IRStatement.followChain(dedupe: Boolean = true, followCond: Boolean = true): Iterable<IRStatement> = Iterable {
    val seen = mutableSetOf<IRStatement>()
    iterator {
        suspend fun SequenceScope<IRStatement>.visit(statement: IRStatement) {
            if (dedupe && statement in seen) return
            yield(statement)
            seen += statement
            statement.next?.let { visit(it) }
            if (followCond) statement.cond?.let { visit(it) }
        }
        visit(this@followChain)
    }
}

/**
 * Replace this [IRStatement] with another, ensuring all nodes pointing to [this] now point to [other].
 *
 * Does **NOT** effect the [IRStatement.next] or [IRStatement.cond] pointers of [other].
 * This means if [other] is `null` this will effectively terminate the chain.
 *
 * @return True if node was successfully replaced or false otherwise (ie you tried to replace a node with itself
 */
fun IRStatement.replaceWith(other: IRStatement?): Boolean {
    // Replace with a nop if we try to replace ourselves, or delete a node that points to itself
    if (other == this || (other == null && next == this)) {
        TODO("How did you even get here? $other $next $this")
//        replace(IRStatement.Nop())
//        return false // Don't count this as a change
    }

    // update next/cond pointers of previous statements
    // make a copy before iterating since this will change while we iterate
    (prev.toList()).forEach {
        if (it.next == this) {
            it.next = other
        } else {
            // prev statement must either have next pointing to this, or be a Conditional with cond pointing to this
            (it as ConditionalStatement).cond = other
        }
    }
    if (prev.isNotEmpty()) throw IllegalStateException("Deleting old references failed!")

    // remove next/cond pointers for this statement
    next = null
    cond = null

    return true
}

fun IRStatement.readParams(): List<IRRegister> = when (this) {
    is IRStatement.WritingStatement -> params.drop(1)
    else -> params
}
