package com.github.scottbot95.stationeers.ic.instructions

import com.github.scottbot95.stationeers.ic.util.toScriptValue

object Misc {
    object Yield : Instruction("yield")
    data class Label(val label: String) : Instruction("$label:")
    data class Comment(val comment: String) : Instruction("#", comment.toScriptValue())
}
