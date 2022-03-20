package com.github.scottbot95.stationeers.ic

sealed class Register(val index: Int) {
    object R0: Register(0)
    object R1: Register(1)
    object R2: Register(2)
    object R3: Register(3)
    object R4: Register(4)
    object R5: Register(5)
    object R6: Register(6)
    object R7: Register(7)
    object R8: Register(8)
    object R9: Register(9)
    object R10: Register(10)
    object R11: Register(11)
    object R12: Register(12)
    object R13: Register(13)
    object R14: Register(14)
    object R15: Register(15)
    object SP: Register(16)
    object RA: Register(17)

    val token = "r$index"

    override fun toString() = token

    companion object {
        private val registers = arrayOf(
            R0, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, SP, RA
        )

        fun getByIndex(index: Int): Register = registers[index]
    }
}