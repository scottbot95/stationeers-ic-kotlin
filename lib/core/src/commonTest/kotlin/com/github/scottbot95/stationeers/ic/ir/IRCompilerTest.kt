package com.github.scottbot95.stationeers.ic.ir

import com.github.scottbot95.stationeers.ic.highlevel.Expression
import com.github.scottbot95.stationeers.ic.highlevel.ICFunction
import com.github.scottbot95.stationeers.ic.highlevel.ICScriptTopLevel
import com.github.scottbot95.stationeers.ic.highlevel.Identifier
import com.github.scottbot95.stationeers.ic.highlevel.Types
import com.github.scottbot95.stationeers.ic.highlevel.toExpr
import com.github.scottbot95.stationeers.ic.testUtils.matchSnapshot
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.should

class IRCompilerTest : WordSpec({
    "Compiler" should {
        "compile matching snapshot" {
            val x = Expression.Ident(Identifier.Parameter("x", 0, Types.Int))
            val a = Expression.Ident(Identifier.Variable("a", 0, Types.Int))

            val functions = listOf(
                ICFunction(
                    "test_loop",
                    Expression.CompoundExpression(
                        Expression.Loop(
                            Expression.Equals(x, 10.toExpr()),
                            Expression.Copy(Expression.Add(x, 1.toExpr()), x)
                        ),
                        Expression.Return(x)
                    ),
                    emptyList()
                ),

                ICFunction(
                    "test_and",
                    Expression.CompoundExpression(
                        Expression.Copy(
                            Expression.Or(
                                Expression.Ident(Identifier.Parameter("x", 0, Types.Int)),
                                Expression.Ident(Identifier.Parameter("y", 1, Types.Int)),
                                Expression.Ident(Identifier.Parameter("z", 2, Types.Int)),
                            ),
                            a
                        ),
                        Expression.Return(a)
                    ),
                    listOf(Types.Int, Types.Int, Types.Int)
                )
            )

            val topLevel = ICScriptTopLevel(functions, Expression.NoOp, emptySet())

            val compilation = IRCompiler.compile(topLevel)

            val dump = compilation.joinToString("\n")
            dump should matchSnapshot
        }
    }
})
