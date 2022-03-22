package com.github.scottbot95.stationeers.ic

sealed interface ScriptValue {

    fun render(context: CompileContext): String

    sealed interface NumberValue : ScriptValue
    sealed interface JumpTarget : ScriptValue
    sealed interface DeviceReference : ScriptValue {
        val device: Device

        override fun render(context: CompileContext): String = device.token
    }
}

sealed interface NumberLiteral<V : Number> : ScriptValue.NumberValue {
    val value: V

    override fun render(context: CompileContext): String = "$value"

    sealed interface IntValue : NumberLiteral<Int>
    sealed interface FloatValue : NumberLiteral<Float>
}


sealed interface RegisterLiteral<V : Number> : ScriptValue.NumberValue {
    val register: Register

    override fun render(context: CompileContext): String = register.token

    sealed interface IntRegisterLiteral : RegisterLiteral<Int>
    sealed interface FloatRegisterLiteral : RegisterLiteral<Float>
}

data class Label(val name: String) : ScriptValue.JumpTarget {
    override fun render(context: CompileContext): String = if (context.options.minify) {
        TODO("Get line number from context")
    } else {
        name
    }
}

data class IntLiteral(override val value: Int) : NumberLiteral.IntValue, ScriptValue.JumpTarget
data class FloatLiteral(override val value: Float) : NumberLiteral.FloatValue

data class IntRegister(override val register: Register) : RegisterLiteral.IntRegisterLiteral, ScriptValue.JumpTarget
data class FloatRegister(override val register: Register) : RegisterLiteral.FloatRegisterLiteral

data class DeviceValue(override val device: Device) : ScriptValue.DeviceReference

data class IntSymbol(val name: String, override val value: Int) : NumberLiteral.IntValue, ScriptValue.JumpTarget
data class FloatSymbol(val name: String, override val value: Float) : NumberLiteral.FloatValue
data class DeviceSymbol(val name: String, override val device: Device) : ScriptValue.DeviceReference
data class IntRegisterSymbol(val name: String, override val register: Register) : RegisterLiteral.IntRegisterLiteral,
    ScriptValue.JumpTarget

data class FloatRegisterSymbol(val name: String, override val register: Register) : RegisterLiteral.FloatRegisterLiteral

// TODO this is maybe not needed
data class ScriptString(val value: String) : ScriptValue {
    override fun render(context: CompileContext): String = value
}
