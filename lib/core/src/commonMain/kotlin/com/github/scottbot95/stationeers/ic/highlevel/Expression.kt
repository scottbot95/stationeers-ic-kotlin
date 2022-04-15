package com.github.scottbot95.stationeers.ic.highlevel

import com.github.scottbot95.stationeers.ic.util.TreeNode

sealed class Expression(label: String, children: List<Expression>) : TreeNode(children, label) {
    constructor(label: String, vararg children: Expression) : this(label, children.toList())
    
    data class IntLiteral(val value: Int) : Expression("$value")

    data class FloatLiteral(val value: Float) : Expression("$value")

    data class Ident(val ident: Identifier) : Expression(ident.name)

    data class Add(val expressions: List<Expression>) : Expression("add", expressions) {
        constructor(vararg expressions: Expression) : this(expressions.toList())
    }

    data class Negate(val expr: Expression) : Expression("neg", expr)

    data class Equals(val a: Expression, val b: Expression) : Expression("equals", a, b)

    data class Or(val left: Expression, val right: Expression) : Expression("or", left, right)
    data class And(val expressions: List<Expression>) : Expression("and", expressions) {
        constructor(vararg expressions: Expression) : this(expressions.toList())
    }

    data class Loop(val condition: Expression, val body: Expression) : Expression("loop", condition, body)

    data class FunctionCall(val function: Ident, val params: List<Expression>) :
        Expression("fcall", function, *params.toTypedArray()) {
        constructor(function: Ident, vararg params: Expression) : this(function, params.toList())
    }

    data class Copy(val source: Expression, val destination: Expression) : Expression("copy", source, destination)

    data class CompoundExpression(val expressions: List<Expression>) : Expression("seq", expressions) {
        constructor(vararg expressions: Expression) : this(expressions.toList())
    }

    data class Return(val expr: Expression) : Expression("ret", expr)
}

fun Int.toExpr(): Expression = Expression.IntLiteral(this)
fun Float.toExpr(): Expression = Expression.FloatLiteral(this)
