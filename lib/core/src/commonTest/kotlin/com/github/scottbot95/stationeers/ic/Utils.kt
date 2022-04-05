package com.github.scottbot95.stationeers.ic

import io.kotest.property.Exhaustive
import io.kotest.property.exhaustive.boolean
import io.kotest.property.exhaustive.map

val compileOptionsExhaustive
    get() = Exhaustive.boolean().map { minify ->
        CompileOptions(minify = minify)
    }
