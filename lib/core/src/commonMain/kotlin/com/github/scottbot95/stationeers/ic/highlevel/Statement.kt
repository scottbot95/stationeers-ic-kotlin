package com.github.scottbot95.stationeers.ic.highlevel

import com.github.scottbot95.stationeers.ic.Location

sealed interface Statement : TopLevelEntry {

    val expression: Expression
    val location: Location?

    data class CompoundStatement(val statements: List<Statement>, override val location: Location? = null) :
        Statement {
        override val expression: Expression =
            Expression.CompoundExpression(statements.map(Statement::expression))
    }

    data class ConditionalStatement(
        val condition: Expression,
        val statement: Statement,
        override val location: Location? = null,
    ) : Statement {
        override val expression: Expression = Expression.And(condition, statement.expression)
    }

    data class LoopStatement(
        val condition: Expression,
        val statement: Statement,
        override val location: Location? = null,
    ) : Statement {
        override val expression: Expression = Expression.Loop(condition, statement.expression)
    }

    data class ExpressionStatement(
        override val expression: Expression,
        override val location: Location? = null
    ) : Statement

}
