package com.github.scottbot95.stationeers.ic

import com.github.scottbot95.stationeers.ic.instructions.Misc

sealed interface ScriptValue<V : Any> {

    fun render(options: CompileOptions): String

    sealed interface NumberValue<V : Number> : ScriptValue<V>
    sealed interface JumpTarget<V : Any> : ScriptValue<V>
    sealed interface DeviceReference : ScriptValue<Device> {
        val device: Device

        override fun render(options: CompileOptions): String = device.token
    }

    sealed interface RegisterValue<V : Number> : ScriptValue<V> {
        val register: Register
    }
}

sealed interface NumberLiteral<V : Number> : ScriptValue.NumberValue<V> {
    val value: V

    override fun render(options: CompileOptions): String = "$value"

    sealed interface IntValue : NumberLiteral<Int>
    sealed interface FloatValue : NumberLiteral<Float>
}

sealed interface RegisterLiteral<V : Number> : ScriptValue.RegisterValue<V>, ScriptValue.NumberValue<V> {
    override fun render(options: CompileOptions): String = register.token

    sealed interface IntRegisterLiteral : RegisterLiteral<Int>
    sealed interface FloatRegisterLiteral : RegisterLiteral<Float>
}

class LineReference(val label: String? = null) : ScriptValue.JumpTarget<Any> {
    private var lineNum: Int? = null

    override fun render(options: CompileOptions): String = if (options.minify || label == null) {
        lineNum?.toString() ?: throw IllegalStateException("LineReference mark was not compiled")
    } else {
        label
    }

    val mark = Compilable { builder ->
        if (lineNum != null) throw IllegalStateException("Cannot compile this LineReference marker in more than one place")

        lineNum = builder.context.nextLineNum

        if (label != null && !builder.context.options.minify) {
            Misc.Label(label).compile(builder)
        }
    }
}

data class IntLiteral(override val value: Int) : NumberLiteral.IntValue, ScriptValue.JumpTarget<Int>
data class FloatLiteral(override val value: Float) : NumberLiteral.FloatValue

data class IntRegister(override val register: Register) :
    RegisterLiteral.IntRegisterLiteral,
    ScriptValue.JumpTarget<Int>

data class FloatRegister(override val register: Register) : RegisterLiteral.FloatRegisterLiteral

data class DeviceLiteral(override val device: Device) : ScriptValue.DeviceReference

sealed interface Symbol<V : Any, SV : ScriptValue<V>> : ScriptValue<V> {
    val name: String
    val mappedValue: SV
}

class IntSymbol(
    override val name: String,
    override val mappedValue: IntLiteral
) : Symbol<Int, IntLiteral>, NumberLiteral.IntValue by mappedValue, ScriptValue.JumpTarget<Int> {
    override fun render(options: CompileOptions): String = renderSymbol(options)
}

class FloatSymbol(
    override val name: String,
    override val mappedValue: FloatLiteral
) : Symbol<Float, FloatLiteral>, NumberLiteral.FloatValue by mappedValue {
    override fun render(options: CompileOptions): String = renderSymbol(options)
}

class DeviceSymbol(
    override val name: String,
    override val mappedValue: ScriptValue.DeviceReference
) : Symbol<Device, ScriptValue.DeviceReference>, ScriptValue.DeviceReference by mappedValue {
    override fun render(options: CompileOptions): String = renderSymbol(options)
}

class IntRegisterSymbol(
    override val name: String,
    override val mappedValue: IntRegister
) : Symbol<Int, IntRegister>, ScriptValue.RegisterValue<Int> by mappedValue, ScriptValue.JumpTarget<Int> {
    override fun render(options: CompileOptions): String = renderSymbol(options)
}

class FloatRegisterSymbol(
    override val name: String,
    override val mappedValue: FloatRegister
) : Symbol<Float, FloatRegister>, ScriptValue.RegisterValue<Float> by mappedValue {
    override fun render(options: CompileOptions): String = renderSymbol(options)
}

// TODO this is maybe not needed
data class StringLiteral(val value: String) : ScriptValue<String> {
    override fun render(options: CompileOptions): String = value
}

private fun <V : Any, SV : ScriptValue<V>> Symbol<V, SV>.renderSymbol(options: CompileOptions): String =
    if (options.minify) {
        mappedValue.render(options)
    } else {
        name
    }
