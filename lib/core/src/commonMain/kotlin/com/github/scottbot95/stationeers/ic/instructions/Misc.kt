package com.github.scottbot95.stationeers.ic.instructions

object Misc {
    object Yield : Instruction("yield")
    data class Label(val label: String) : Instruction("$label:")
}
