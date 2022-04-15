package com.github.scottbot95.stationeers.ic

expect fun sourceLocation(depth: Int = 0): Location?

data class Location(
    val filename: String,
    val lineNum: Int,
)
