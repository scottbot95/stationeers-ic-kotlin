package com.github.scottbot95.stationeers.ic.dsl

internal fun compiledScript(init: PartialCompiledScript.Builder.() -> Unit): PartialCompiledScript =
    PartialCompiledScript.Builder()
        .apply(init)
        .build()

internal fun PartialCompiledScript.builder(init: PartialCompiledScript.Builder.() -> Unit): PartialCompiledScript = builder().apply(init).build()
