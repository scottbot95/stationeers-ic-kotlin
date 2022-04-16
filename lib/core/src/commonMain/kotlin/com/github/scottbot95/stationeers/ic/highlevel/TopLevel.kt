package com.github.scottbot95.stationeers.ic.highlevel

import com.github.scottbot95.stationeers.ic.highlevel.optimization.Optimizer
import com.github.scottbot95.stationeers.ic.util.depthFirst

data class ICScriptTopLevel(val functions: List<ICFunction>, val code: Expression, val globals: Set<Identifier>)

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
                            .any { it is Expression.Ident && it.ident in globals }
                        is Expression.FunctionCall -> when {
                            !expr.function.isCompileTimeExpr -> true
                            else -> {
                                val function = functions[expr.function.ident.index]
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


sealed interface TopLevelEntry

data class ICFunction(
    val name: String,
    val code: Expression,
    val paramTypes: List<Types.Any>,
    var pure: Boolean? = null, // TODO ew gross mutable properties!
) : TopLevelEntry

// TODO these names all collide with base kotlin types. Should we avoid that? Prefix all with IC?
abstract class Types {
    override fun toString(): String = ""

    abstract class Any : Types() {
        companion object : Any()
    }

    object Unit : Any()

    abstract class Number : Any() {
        companion object : Number()
    }

    object Int : Number()
    object Float : Number()
}
