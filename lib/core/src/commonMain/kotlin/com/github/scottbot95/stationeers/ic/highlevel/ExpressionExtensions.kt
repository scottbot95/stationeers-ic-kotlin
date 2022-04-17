package com.github.scottbot95.stationeers.ic.highlevel

fun Int.toExpr(): Expression = Expression.IntLiteral(this)
fun Float.toExpr(): Expression = Expression.FloatLiteral(this)
