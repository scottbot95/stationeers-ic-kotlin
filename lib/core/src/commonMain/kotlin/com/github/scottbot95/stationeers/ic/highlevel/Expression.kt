package com.github.scottbot95.stationeers.ic.highlevel

import com.github.scottbot95.stationeers.ic.util.TreeNode

sealed class Expression(label: String, children: List<Expression>) : TreeNode<Expression>(children, label) {
    constructor(label: String, vararg children: Expression) : this(label, children.toList())

    val isCompileTimeExpr: Boolean by lazy {
        children.all { it.isCompileTimeExpr } && when (this) {
            is NumberLiteral<*>, is Add, is Negate, is And, is Or, is CompoundExpression, is NoOp -> true
            is Ident -> ident is Identifier.Function
            else -> false
        }
    }

    // TODO only need context arg for funcs. Can we be more clever to remove this and make it a simple property?
    // Most things are pure so default to pure
    open fun isPure(context: ICScriptContext): Boolean = true

    object NoOp : Expression("nop") {
        override fun copy(children: List<Expression>, label: String): Expression = NoOp
    }

    sealed class NumberLiteral<T : Number>(label: String) : Expression(label) {
        abstract val value: T
    }

    data class IntLiteral(override val value: Int) : NumberLiteral<Int>("$value") {
        override fun copy(children: List<Expression>, label: String): Expression = IntLiteral(value)
    }

    data class FloatLiteral(override val value: Float) : NumberLiteral<Float>("$value") {
        override fun copy(children: List<Expression>, label: String): Expression = FloatLiteral(value)

    }

    data class Ident(val ident: Identifier) : Expression(ident.name) {
        override fun copy(children: List<Expression>, label: String): Expression = Ident(ident)
    }

    data class Add(val expressions: List<Expression>) : Expression("add", expressions) {
        constructor(vararg expressions: Expression) : this(expressions.toList())

        override fun copy(children: List<Expression>, label: String): Expression = Add(children)
    }

    data class Negate(val expr: Expression) : Expression("neg", expr) {
        override fun copy(children: List<Expression>, label: String): Expression = Negate(children.first())
    }

    data class Equals(val a: Expression, val b: Expression) : Expression("equals", a, b) {
        override fun copy(children: List<Expression>, label: String): Expression = Equals(children[0], children[1])
    }

    data class Or(val expressions: List<Expression>) : Expression("or", expressions) {
        constructor(vararg expressions: Expression) : this(expressions.toList())

        override fun copy(children: List<Expression>, label: String): Expression = Or(children)
    }

    data class And(val expressions: List<Expression>) : Expression("and", expressions) {
        constructor(vararg expressions: Expression) : this(expressions.toList())

        override fun copy(children: List<Expression>, label: String): Expression = And(children)

    }

    data class Loop(val condition: Expression, val body: Expression) : Expression("loop", condition, body) {
        override fun copy(children: List<Expression>, label: String): Expression = Loop(children[0], children[1])

        // Loops are not pure, because they may be infinite,
        // in which case deleting the loop would alter the program behavior.
        override fun isPure(context: ICScriptContext): Boolean = false
    }

    data class FunctionCall(val function: Ident, val params: List<Expression>) :
        Expression("fcall", function, *params.toTypedArray()) {
        constructor(function: Ident, vararg params: Expression) : this(function, params.toList())

        override fun copy(children: List<Expression>, label: String): Expression =
            FunctionCall(children[0] as Ident, children[1])

        override fun isPure(context: ICScriptContext): Boolean {
            val icFunction = context.functions.getOrNull(function.ident.index)
            if (icFunction?.name == function.ident.name) {
                return icFunction.pure == true
            }

            return false
        }
    }

    data class Copy(val source: Expression, val destination: Ident) : Expression("copy", source, destination) {
        override fun copy(children: List<Expression>, label: String): Expression =
            Copy(children[0], children[1] as Ident)

        // Assigns are not pure.
        override fun isPure(context: ICScriptContext): Boolean = false
    }

    data class CompoundExpression(val expressions: List<Expression>) : Expression("seq", expressions) {
        constructor(vararg expressions: Expression) : this(expressions.toList())

        override fun copy(children: List<Expression>, label: String): Expression = CompoundExpression(children)
    }

    data class Return(val expr: Expression) : Expression("ret", expr) {
        override fun copy(children: List<Expression>, label: String): Expression = Return(children.first())

        // Returns and not pure because they do not evaluate into a value.
        // TODO can we copy the Nothing principal from Kotlin somehow?
        override fun isPure(context: ICScriptContext): Boolean = false
    }
}
