package com.github.scottbot95.stationeers.ic.highlevel

data class ICScriptTopLevel(val functions: List<ICFunction>, val code: Expression)

sealed interface TopLevelEntry

data class ICFunction(
    val name: String,
    val code: Expression,
    val paramTypes: List<NumberType>,
) : TopLevelEntry

enum class NumberType {
    INT, FLOAT
}
