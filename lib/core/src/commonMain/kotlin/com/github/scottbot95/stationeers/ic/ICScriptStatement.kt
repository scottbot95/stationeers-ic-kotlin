package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.instructions.Instruction

interface ICScriptStatement {

    val instruction: Instruction?

    /**
     * @return The [String] representation of this [ICScriptStatement]
     */
    override fun toString(): String

    object EMPTY : ICScriptStatement {
        override val instruction: Instruction? = null

        override fun toString(): String = ""
    }
}
