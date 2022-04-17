package com.github.scottbot95.stationeers.ic.highlevel

import com.github.scottbot95.stationeers.ic.SyntaxException

data class ICScriptContext(
    val scopes: MutableList<MutableMap<String, Identifier>> = mutableListOf(mutableMapOf()),
    val functions: MutableList<ICFunction> = mutableListOf(),
    var tempCounter: UInt = 0U
)

inline val ICScriptContext.curScope get() = scopes.last()

private fun ICScriptContext.define(ident: Identifier): Expression.Ident {
    val curScope = curScope
    if (curScope.contains(ident.name)) throw SyntaxException("Duplicate definition for <${ident.name}>. Already defined as ${curScope[ident.name]}")
    curScope[ident.name] = ident
    return Expression.Ident(ident)
}

fun ICScriptContext.defVar(name: String, type: Types.Any) =
    define(Identifier.Variable(name, curScope.count { it.value is Identifier.Variable }, type))

fun ICScriptContext.defParam(name: String, type: Types.Any) =
    define(Identifier.Parameter(name, curScope.count { it.value is Identifier.Parameter }, type))

fun ICScriptContext.defFunc(name: String, returnType: Types.Any) =
    define(Identifier.Function(name, functions.size, returnType))

fun ICScriptContext.temp(type: Types.Any) = defVar("\$${"$type".first()}${tempCounter++}", type)

fun ICScriptContext.use(name: String) = scopes.firstNotNullOfOrNull { it[name] }?.let { Expression.Ident(it) }

fun ICScriptContext.addFunction(name: String, paramTypes: List<Types.Any>, code: () -> Expression) {
    scopes.add(mutableMapOf())
    val funcCode = Expression.CompoundExpression(code(), Expression.Return(Types.Unit.toExpr()))
    val func = ICFunction(name, funcCode, paramTypes)
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
