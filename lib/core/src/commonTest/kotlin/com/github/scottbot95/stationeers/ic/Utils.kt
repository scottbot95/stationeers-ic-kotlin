package com.github.scottbot95.stationeers.ic

import io.kotest.property.Exhaustive
import io.kotest.property.exhaustive.boolean
import io.kotest.property.exhaustive.map

fun Exhaustive.Companion.compileOptions() = Exhaustive.boolean().map { minify ->
    CompileOptions(minify = minify)
}
