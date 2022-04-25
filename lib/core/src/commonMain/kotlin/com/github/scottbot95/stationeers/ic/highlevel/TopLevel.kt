package com.github.scottbot95.stationeers.ic.highlevel

import com.github.scottbot95.stationeers.ic.highlevel.optimization.Optimizer
import com.github.scottbot95.stationeers.ic.ir.IRCompilation
import com.github.scottbot95.stationeers.ic.ir.IRCompileContext
import com.github.scottbot95.stationeers.ic.ir.IRFunction
import com.github.scottbot95.stationeers.ic.ir.IRStatement
import com.github.scottbot95.stationeers.ic.ir.plusAssign
import com.github.scottbot95.stationeers.ic.util.compareWith
import com.github.scottbot95.stationeers.ic.util.depthFirst
import com.github.scottbot95.stationeers.ic.util.toTreeString

sealed interface TopLevelEntry

data class ICScriptTopLevel(
    val functions: List<ICFunction>,
    val code: Expression,
    val globals: Set<Identifier>
) {
    constructor(context: ICScriptContext, code: () -> Expression = { Expression.NoOp }) : this(
        context.functions,
        code(),
        context.scopes.first().values.toSet()
    )

    fun compile(): IRCompilation {
        val topLevelPlaceholder = IRStatement.Placeholder()
        val topLevelContext = IRCompileContext(
            regCount = 0U,
            next = topLevelPlaceholder::next
        )
        code.compile(topLevelContext)
        // slap a halt at the end of the top level to prevent overrun into the function code
        // Will be optimized away if unnecessary (eg: user already added a halt or inifite loop)
        topLevelContext += IRStatement.Halt()

        val functions = functions.associate {
            val numParams = it.paramTypes.size
            val entrypointPlaceholder = IRStatement.Placeholder()
            val context = IRCompileContext(
                regCount = (topLevelContext.variables.size + numParams).toUInt(),
                globals = topLevelContext.variables, // top-level vars are effectively globals
                next = entrypointPlaceholder::next
            )
            it.code.compile(context)
            it.name to IRFunction(it.name, entrypointPlaceholder.next!!, numParams)
        }

        return IRCompilation(functions, topLevelPlaceholder.next!!)
    }
}

fun ICScriptTopLevel.toTreeString(): String {
    val sb = StringBuilder()
    functions.forEach {
        sb.appendLine("function ${it.name}:")
        sb.appendLine(it.code.toTreeString())
    }

    sb.appendLine("top level:")
    sb.append(code.toTreeString())

    return "$sb"
}

fun ICScriptTopLevel.compareTreeStrings(other: ICScriptTopLevel): String {
    return toTreeString().compareWith(other.toTreeString())
}

fun ICScriptTopLevel.optimize(): ICScriptTopLevel {
    val optimizer = Optimizer()

    return optimizer.optimizeTopLevel(this)
}

internal fun ICScriptTopLevel.updatePureFunctions(): ICScriptTopLevel {
    // reset pure-status
    functions.forEach {
        // keep pure status if already marked pure, otherwise reset to null
        // DEAR FUTURE DEV: I have no idea if this is actually safe to do, but it sounds reasonable that
        //                  once a function is pure, it cannot be optimized into being impure. Don't @ me
        it.pure = if (it.pure == true) true else null
    }

    // loop until all functions are marked as pure/impure
    while (functions.any { it.pure == null }) {
        functions.forEach { func ->
            if (func.pure == null) {
                // The function has side effects if any of the following are true
                // - there is a global variable ident in the LHS of an assign operator
                // - (future) the function contains a device write operation
                // - (future) the function contains a device read operation
                //              (this is technically only impure across tickets, maybe we can optimize?)
                // - the function calls some other function that is known to have side effects
                var unknownFunctions = false
                val hasSideEffects = func.code.depthFirst().any { expr ->
                    when (expr) {
                        is Expression.Copy -> expr.destination.depthFirst()
                            .any { it is Expression.Ident && it.id in globals }
                        is Expression.FunctionCall -> when {
                            !expr.function.isCompileTimeExpr -> true
                            else -> {
                                val function = functions[expr.function.id.index]
                                if (function.pure == false) {
                                    true
                                } else {
                                    if (function.pure == null && function != func) {
                                        unknownFunctions = true
                                    }
                                    false
                                }
                            }
                        }
                        else -> false
                    }
                }

                // if found side effects, mark impure. If nothing found, and all called functions are known pure, also mark pure
                if (hasSideEffects || !unknownFunctions) {
                    func.pure = !hasSideEffects
                }
            }
        }
    }

    return this
}
