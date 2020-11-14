package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.RegisterValue
import com.github.scottbot95.stationeers.ic.util.AliasedScriptValueContainer

class RegisterContainer : AliasedScriptValueContainer<Register>() {
    override fun nextFreeValue(): Register? = Register.values().firstOrNull { getUsed(it) == 0 }

    override fun newInstance(value: Register): ScriptValue<Register> = RegisterValue(value)
}
