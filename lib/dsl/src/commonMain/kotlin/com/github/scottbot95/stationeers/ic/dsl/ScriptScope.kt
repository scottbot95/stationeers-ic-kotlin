package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.CompileOptions
import com.github.scottbot95.stationeers.ic.ICScript
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@ExperimentalContracts
fun script(init: ScriptScope.() -> Unit): ICScript {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }

    TODO()
}

@ICDsl
interface ScriptScope {
    var compileOptions: CompileOptions
}
