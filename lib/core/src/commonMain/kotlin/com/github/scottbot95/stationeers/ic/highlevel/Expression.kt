package com.github.scottbot95.stationeers.ic.highlevel

import com.github.scottbot95.stationeers.ic.ir.IRCompileContext
import com.github.scottbot95.stationeers.ic.ir.IRRegister
import com.github.scottbot95.stationeers.ic.ir.IRStatement
import com.github.scottbot95.stationeers.ic.ir.makeReg
import com.github.scottbot95.stationeers.ic.ir.plusAssign
import com.github.scottbot95.stationeers.ic.ir.withReg
import com.github.scottbot95.stationeers.ic.util.TreeNode

sealed class Expression(label: String, children: List<Expression>) : TreeNode<Expression>(children, label) {
    constructor(label: String, vararg children: Expression) : this(label, children.toList())

    val isCompileTimeExpr: Boolean by lazy {
        children.all { it.isCompileTimeExpr } && when (this) {
            is NumberLiteral<*>, is Add, is Negate, is And, is Or, is CompoundExpression, is NoOp -> true
            is Ident -> id is Identifier.Function
            else -> false
        }
    }

    // TODO only need context arg for funcs. Can we be more clever to remove this and make it a simple property?
    // Most things are pure so default to pure
    open fun isPure(context: ICScriptContext): Boolean = children.all { it.isPure(context) }

    abstract fun compile(context: IRCompileContext): IRRegister

    object NoOp : Expression("nop") {
        override fun copy(children: List<Expression>, label: String): Expression = NoOp
        override fun toString(): String = "NoOp"
        override fun compile(context: IRCompileContext): IRRegister =
            context.withReg { tmp ->
                // TODO is there a reason we can't use NoOp here?
                context += IRStatement.Init(tmp, null, 0) // dummy expr. Will get optimized away
            }
    }

    sealed class NumberLiteral<T : Number>(label: String) : Expression(label) {
        abstract val value: T

        override fun compile(context: IRCompileContext): IRRegister =
            context.withReg { tmp ->
                context += IRStatement.Init(tmp, null, value)
            }
    }

    data class IntLiteral(override val value: Int) : NumberLiteral<Int>("$value") {
        override fun copy(children: List<Expression>, label: String): Expression = IntLiteral(value)
    }

    data class FloatLiteral(override val value: Float) : NumberLiteral<Float>("$value") {
        override fun copy(children: List<Expression>, label: String): Expression = FloatLiteral(value)
    }

    data class Ident(val id: Identifier) : Expression(id.name) {
        override fun compile(context: IRCompileContext): IRRegister = when (id) {
            is Identifier.Function -> context.withReg { IRStatement.Init(it, id.name, 0) }
            is Identifier.Parameter -> IRRegister((context.globals.size + id.index).toUInt())
            is Identifier.Variable -> context.variables.getOrPut(id.name) { context.makeReg() }
        }

        override fun copy(children: List<Expression>, label: String): Expression = Ident(id)
    }

    data class Add(val expressions: List<Expression>) : Expression("add", expressions) {
        constructor(vararg expressions: Expression) : this(expressions.toList())

        override fun compile(context: IRCompileContext): IRRegister =
            // We can assume there is at least two arguments or the AST optimizer would have reduced it
            expressions.drop(1).fold(expressions.first().compile(context)) { acc, expr ->
                context.withReg { dest ->
                    context += IRStatement.Add(dest, acc, expr.compile(context))
                }
            }

        override fun copy(children: List<Expression>, label: String): Expression = Add(children)
    }

    data class Negate(val expr: Expression) : Expression("neg", expr) {
        override fun compile(context: IRCompileContext): IRRegister = context.withReg { reg ->
            context += IRStatement.Negate(reg, expr.compile(context))
        }

        override fun copy(children: List<Expression>, label: String): Expression = Negate(children.first())
    }

    data class Equals(val a: Expression, val b: Expression) : Expression("equals", a, b) {
        override fun compile(context: IRCompileContext): IRRegister = context.withReg { dest ->
            context += IRStatement.Equals(dest, a.compile(context), b.compile(context))
        }

        override fun copy(children: List<Expression>, label: String): Expression = Equals(children[0], children[1])
    }

    sealed class ConditionalExpression(
        label: String,
        private val conditions: List<Expression>,
        private val body: Expression? = null,
        private val isAnd: Boolean = true, // also counts loops. Maybe needs better name
    ) : Expression(label, conditions + listOfNotNull(body)) {
        override fun compile(context: IRCompileContext): IRRegister = context.withReg { finalResult ->
            val thenBranch = IRStatement.Init(finalResult, "", if (isAnd) 1 else 0)
            val elseBranch = IRStatement.Init(finalResult, "", if (isAnd) 0 else 1)
            val end = IRStatement.Nop().apply {
                thenBranch.next = this
                elseBranch.next = this
            }
            val begin = context.next

            conditions.forEach { expr ->
                val result = expr.compile(context)
                val branch = if (isAnd) {
                    IRStatement.IfZero(result)
                } else {
                    IRStatement.IfNotZero(result)
                }.apply {
                    cond = elseBranch
                }

                context += branch
            }

            if (body != null) {
                body.compile(context)
                context.next.set(begin.get())
            } else {
                // append the then clause to set our result register
                context += thenBranch
            }

            // make sure end statement is set in context
            context.next = end::next
        }
    }

    data class Or(val expressions: List<Expression>) : ConditionalExpression("or", expressions, isAnd = false) {

        constructor(vararg expressions: Expression) : this(expressions.toList())

        override fun copy(children: List<Expression>, label: String): Expression = Or(children)
    }

    data class And(val expressions: List<Expression>) : ConditionalExpression("and", expressions) {
        constructor(vararg expressions: Expression) : this(expressions.toList())

        override fun copy(children: List<Expression>, label: String): Expression = And(children)
    }

    data class Loop(val condition: Expression, val body: Expression) :
        ConditionalExpression("loop", listOf(condition), body) {
        override fun copy(children: List<Expression>, label: String): Expression = Loop(children[0], children[1])

        // Loops are not pure, because they may be infinite,
        // in which case deleting the loop would alter the program behavior.
        override fun isPure(context: ICScriptContext): Boolean = false
    }

    data class FunctionCall(val function: Ident, val params: List<Expression>) :
        Expression("fcall", function, *params.toTypedArray()) {
        constructor(function: Ident, vararg params: Expression) : this(function, params.toList())

        override fun copy(children: List<Expression>, label: String): Expression =
            FunctionCall(children[0] as Ident, children.drop(1))

        override fun isPure(context: ICScriptContext): Boolean {
            val icFunction = context.functions.getOrNull(function.id.index)
            if (icFunction?.name == function.id.name) {
                return super.isPure(context) && icFunction.pure == true
            }

            throw IllegalStateException("found function name does not match expected name. Found: ${icFunction?.name} Expected: ${function.id.name}")
        }

        override fun compile(context: IRCompileContext): IRRegister = context.withReg { result ->
            context += IRStatement.FunctionCall(result, function.compile(context), params.map { it.compile(context) })
        }
    }

    data class Copy(val source: Expression, val destination: Ident) : Expression("copy", source, destination) {
        override fun copy(children: List<Expression>, label: String): Expression =
            Copy(children[0], children[1] as Ident)

        // Assigns are not pure.
        override fun isPure(context: ICScriptContext): Boolean = false

        override fun compile(context: IRCompileContext): IRRegister {
            val src = source.compile(context)
            val dest = destination.compile(context)
            context += IRStatement.Copy(dest, src)
            return dest
        }
    }

    data class CompoundExpression(val expressions: List<Expression>) : Expression("seq", expressions) {
        constructor(vararg expressions: Expression) : this(expressions.toList())

        override fun compile(context: IRCompileContext): IRRegister =
            // Should be fine to make a useless register here
            expressions.fold(context.makeReg()) { _, expr -> expr.compile(context) }

        override fun copy(children: List<Expression>, label: String): Expression = CompoundExpression(children)
    }

    data class Return(val expr: Expression) : Expression("ret", expr) {
        override fun copy(children: List<Expression>, label: String): Expression = Return(children.first())

        // Returns are not pure because they do not evaluate into a value.
        // TODO can we copy the Nothing principal from Kotlin somehow?
        override fun isPure(context: ICScriptContext): Boolean = false

        override fun compile(context: IRCompileContext): IRRegister = expr.compile(context).also { result ->
            context += IRStatement.Return(result)
        }
    }
}
