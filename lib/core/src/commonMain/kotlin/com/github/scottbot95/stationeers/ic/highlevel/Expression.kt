package com.github.scottbot95.stationeers.ic.highlevel

import com.github.scottbot95.stationeers.ic.util.TreeNode

sealed class Expression(label: String, children: List<Expression>) : TreeNode<Expression>(children, label) {
    constructor(label: String, vararg children: Expression) : this(label, children.toList())

    fun isPure(): Boolean = TODO()

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

    data class Or(val left: Expression, val right: Expression) : Expression("or", left, right) {
        override fun copy(children: List<Expression>, label: String): Expression = Or(children[0], children[1])

    }

    data class And(val expressions: List<Expression>) : Expression("and", expressions) {
        constructor(vararg expressions: Expression) : this(expressions.toList())

        override fun copy(children: List<Expression>, label: String): Expression = And(children)

    }

    data class Loop(val condition: Expression, val body: Expression) : Expression("loop", condition, body) {
        override fun copy(children: List<Expression>, label: String): Expression = Loop(children[0], children[1])

    }

    data class FunctionCall(val function: Ident, val params: List<Expression>) :
        Expression("fcall", function, *params.toTypedArray()) {
        constructor(function: Ident, vararg params: Expression) : this(function, params.toList())

        override fun copy(children: List<Expression>, label: String): Expression =
            FunctionCall(children[0] as Ident, children[1])
    }

    data class Copy(val source: Expression, val destination: Expression) : Expression("copy", source, destination) {
        override fun copy(children: List<Expression>, label: String): Expression = Copy(children[0], children[1])
    }

    data class CompoundExpression(val expressions: List<Expression>) : Expression("seq", expressions) {
        constructor(vararg expressions: Expression) : this(expressions.toList())

        override fun copy(children: List<Expression>, label: String): Expression = CompoundExpression(children)
    }

    data class Return(val expr: Expression) : Expression("ret", expr) {
        override fun copy(children: List<Expression>, label: String): Expression = Return(children.first())
    }
}
