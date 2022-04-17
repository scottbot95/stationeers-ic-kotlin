package com.github.scottbot95.stationeers.ic.highlevel

data class ICFunction(
    val name: String,
    val code: Expression,
    val paramTypes: List<Types.Any>,
    var pure: Boolean? = null, // TODO ew gross mutable properties!
) : TopLevelEntry