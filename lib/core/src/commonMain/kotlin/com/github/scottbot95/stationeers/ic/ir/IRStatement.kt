package com.github.scottbot95.stationeers.ic.ir

import com.github.scottbot95.stationeers.ic.ir.IRStatement.ConditionalStatement
import kotlin.reflect.KMutableProperty0

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
            set(value) {
                updateReference(value, field, ::cond)
                field = value
            }

        override fun toString(): String = "$opCode $check $jumpLabel"
    }

    /**
     * Reference to the next statement after this one in the "false" case of branches (default)
     */
    var next: IRStatement? = null
        set(value) {
            updateReference(value, field, ::next)
            field = value
        }

    internal val prev: MutableList<KMutableProperty0<IRStatement?>> = mutableListOf()

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

private fun updateReference(
    new: IRStatement?,
    old: IRStatement?,
    self: KMutableProperty0<IRStatement?>
) {
    // This used to point to something, but now it doesn't. Delete the old prev reference
    old?.let {
        it.prev -= self
    }
    if (new != null) {
        // Add this to next statements prev references
        new.prev += self
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
fun IRStatement.replace(other: IRStatement?): Boolean {
    // Replace with a nop if we try to replace ourselves, or delete a node that points to itself
    if (other == this || (other == null && next == this)) {
        replace(IRStatement.Nop())
        return false // Don't count this as a change
    }

    // update next/cond pointers of previous statements
    // make a copy before iterating since this will change while we iterate
    (prev.toList()).forEach {
        it.set(other)
    }
    if (prev.isNotEmpty()) throw IllegalStateException("Deleting old references failed!")

    // remove next/cond pointers for this statement
    next = null
    cond = null

    return true
}
