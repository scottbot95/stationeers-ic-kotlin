package com.github.scottbot95.stationeers.ic.dsl

fun compiledScript(init: CompiledScript.Builder.() -> Unit): CompiledScript =
    CompiledScript.Builder()
        .apply(init)
        .build()

fun CompiledScript.builder(init: CompiledScript.Builder.() -> Unit): CompiledScript = builder().apply(init).build()
