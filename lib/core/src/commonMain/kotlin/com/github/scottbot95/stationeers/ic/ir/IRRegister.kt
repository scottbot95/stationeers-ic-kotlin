package com.github.scottbot95.stationeers.ic.ir

/**
 * Register for the intermediate representation. Not to be confused with
 * the "physical" registers on an IC10 chip
 */
data class IRRegister(val id: UInt) {
    override fun toString(): String = "R$id"
}