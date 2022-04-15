package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.highlevel.Expression
import com.github.scottbot95.stationeers.ic.highlevel.ICScriptContext
import com.github.scottbot95.stationeers.ic.highlevel.NumberType
import com.github.scottbot95.stationeers.ic.highlevel.Statement
import com.github.scottbot95.stationeers.ic.highlevel.toExpr
import com.github.scottbot95.stationeers.ic.sourceLocation

data class ICScriptBlockScopeContext(
    val scriptContext: ICScriptContext,
    var currentScope: ICScriptBlockScope? = null
)

@ICScriptDSL
interface ICScriptBlockScope {
    val parent: ICScriptBlockScope?
    val context: ICScriptBlockScopeContext
    val statements: List<Statement>

    operator fun Statement.unaryPlus()
    operator fun Expression.unaryPlus() = +Statement.ExpressionStatement(this)

    fun block(init: ICScriptBlockScope.() -> Unit)
    fun cond(condition: Expression, init: ICScriptBlockScope.() -> Unit)
    fun loop(condition: Expression, init: ICScriptBlockScope.() -> Unit)

    operator fun Expression.plus(other: Expression): Expression = Expression.Add(this, other)
    operator fun Expression.minus(other: Expression): Expression = this + Expression.Negate(other)
    operator fun Expression.unaryMinus(): Expression = Expression.Negate(this)

    // TODO For some reason kotlin doesn't like these method signatures
    // ++/-- have weird implications as well since it is non-trivial to detect pre-increment vs post increment
//    operator fun Expression.dec(): Expression = Expression.NoOp
//    operator fun Expression.inc(): Expression = Expression.NoOp

    fun Expression.eq(other: Expression): Expression = Expression.Equals(this, other)
    fun Expression.neq(other: Expression): Expression = Expression.Equals(Expression.Equals(this, other), 0.toExpr())

    operator fun Expression.not(): Expression = Expression.Equals(this, 0.toExpr())
    fun Expression.or(other: Expression): Expression = Expression.Or(this, other)
    fun Expression.and(other: Expression): Expression = Expression.And(this, other)

    fun int(name: String? = null) = VariableDelegateProvider(this, name, NumberType.INT)
}

internal open class ICScriptBlockContainer(
    override val context: ICScriptBlockScopeContext,
    override val parent: ICScriptBlockScope? = null,
) : ICScriptBlockScope {

    override val statements: MutableList<Statement> = mutableListOf()

    override fun Statement.unaryPlus() {
        statements += this
    }

    override fun block(init: ICScriptBlockScope.() -> Unit) {
        +buildBlock(init)
    }

    override fun cond(condition: Expression, init: ICScriptBlockScope.() -> Unit) {
        val statement = buildBlock(init)
        +Statement.ConditionalStatement(condition, statement, statement.location)
    }

    override fun loop(condition: Expression, init: ICScriptBlockScope.() -> Unit) {
        val statement = buildBlock(init)
        +Statement.LoopStatement(condition, statement, statement.location)
    }

    private fun buildBlock(init: ICScriptBlockScope.() -> Unit): Statement {
        val prevScope = context.currentScope
        val newBlock = ICScriptBlockContainer(context, this)
        context.currentScope = newBlock
        init(newBlock)
        context.currentScope = prevScope
        return Statement.CompoundStatement(newBlock.statements, sourceLocation(2))
    }
}
