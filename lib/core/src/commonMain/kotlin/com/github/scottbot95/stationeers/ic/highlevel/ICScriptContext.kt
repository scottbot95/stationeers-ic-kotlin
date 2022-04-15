package com.github.scottbot95.stationeers.ic.highlevel

import com.github.scottbot95.stationeers.ic.SyntaxException

data class ICScriptContext(
    val scopes: MutableList<MutableMap<String, Identifier>> = mutableListOf(mutableMapOf()),
    val functions: MutableList<ICFunction> = mutableListOf(),
    var tempCounter: UInt = 0U
)

inline val ICScriptContext.curScope get() = scopes.last()

private fun <T : Identifier> ICScriptContext.define(ident: T): T {
    val curScope = curScope
    if (curScope.contains(ident.name)) throw SyntaxException("Duplicate definition <${ident.name}>")
    curScope[ident.name] = ident
    return ident
}

fun ICScriptContext.defVar(name: String, type: NumberType) =
    define(Identifier.Variable(name, curScope.count { it.value is Identifier.Variable }, type))

fun ICScriptContext.defParam(name: String, type: NumberType) =
    define(Identifier.Parameter(name, curScope.count { it.value is Identifier.Parameter }, type))

fun ICScriptContext.defFunc(name: String, returnType: NumberType?) =
    define(Identifier.Function(name, functions.size, returnType))

fun ICScriptContext.temp(type: NumberType) = defVar("\$${type.name.first()}${tempCounter++}", type)

fun ICScriptContext.use(name: String) = scopes.firstNotNullOfOrNull { it[name] }

fun ICScriptContext.addFunction(name: String, paramTypes: List<NumberType>, code: () -> Expression) {
    scopes.add(mutableMapOf())
    val func = ICFunction(name, code(), paramTypes)
    scopes.removeLast()
    functions += func
}

operator fun ICScriptContext.inc(): ICScriptContext {
    scopes.add(mutableMapOf())
    return this
}

operator fun ICScriptContext.dec(): ICScriptContext {
    scopes.removeLast()
    return this
}
