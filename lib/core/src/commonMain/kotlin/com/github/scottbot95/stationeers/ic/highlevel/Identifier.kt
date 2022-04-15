package com.github.scottbot95.stationeers.ic.highlevel

sealed interface Identifier {
    val name: String
    val index: Int

    data class Function(override val name: String, override val index: Int, val returnType: NumberType?) : Identifier
    data class Parameter(override val name: String, override val index: Int, val type: NumberType) : Identifier
    data class Variable(override val name: String, override val index: Int, val type: NumberType) : Identifier
}