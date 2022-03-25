package com.github.scottbot95.stationeers.ic

data class CompiledScriptValue<out SV : ScriptValue<*>>(
    val value: SV,
    val rendered: String,
) {
    override fun toString(): String = rendered
}

sealed interface ScriptValue<V : Any> {

    fun compile(context: CompileContext): CompiledScriptValue<ScriptValue<V>>

    sealed interface NumberValue<V : Number> : ScriptValue<V>
    sealed interface JumpTarget<V : Any> : ScriptValue<V>
    sealed interface DeviceReference : ScriptValue<Device> {
        val device: Device

        override fun compile(context: CompileContext): CompiledScriptValue<ScriptValue<Device>> = render(device.token)
    }

    sealed interface RegisterValue<V : Number> : ScriptValue<V> {
        val register: Register
    }
}

sealed interface NumberLiteral<V : Number> : ScriptValue.NumberValue<V> {
    val value: V

    override fun compile(context: CompileContext): CompiledScriptValue<ScriptValue<V>> = render("$value")

    sealed interface IntValue : NumberLiteral<Int>
    sealed interface FloatValue : NumberLiteral<Float>
}


sealed interface RegisterLiteral<V : Number> : ScriptValue.RegisterValue<V>, ScriptValue.NumberValue<V> {
    override fun compile(context: CompileContext): CompiledScriptValue<ScriptValue<V>> = render(register.token)

    sealed interface IntRegisterLiteral : RegisterLiteral<Int>
    sealed interface FloatRegisterLiteral : RegisterLiteral<Float>
}

data class Label(val name: String) : ScriptValue.JumpTarget<String> {
    override fun compile(context: CompileContext): CompiledScriptValue<Label> = if (context.options.minify) {
        TODO("Get line number from context")
    } else {
        render(name)
    }
}

data class IntLiteral(override val value: Int) : NumberLiteral.IntValue, ScriptValue.JumpTarget<Int>
data class FloatLiteral(override val value: Float) : NumberLiteral.FloatValue

data class IntRegister(override val register: Register) : RegisterLiteral.IntRegisterLiteral,
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
    override fun compile(context: CompileContext): CompiledScriptValue<ScriptValue<Int>> = renderSymbol(context)
}

class FloatSymbol(
    override val name: String,
    override val mappedValue: FloatLiteral
) : Symbol<Float, FloatLiteral>, NumberLiteral.FloatValue by mappedValue {
    override fun compile(context: CompileContext): CompiledScriptValue<ScriptValue<Float>> = renderSymbol(context)
}

class DeviceSymbol(
    override val name: String,
    override val mappedValue: ScriptValue.DeviceReference
) : Symbol<Device, ScriptValue.DeviceReference>, ScriptValue.DeviceReference by mappedValue {
    override fun compile(context: CompileContext): CompiledScriptValue<ScriptValue<Device>> = renderSymbol(context)
}

class IntRegisterSymbol(
    override val name: String,
    override val mappedValue: IntRegister
) : Symbol<Int, IntRegister>, ScriptValue.RegisterValue<Int> by mappedValue, ScriptValue.JumpTarget<Int> {
    override fun compile(context: CompileContext): CompiledScriptValue<ScriptValue<Int>> = renderSymbol(context)
}

class FloatRegisterSymbol(
    override val name: String,
    override val mappedValue: FloatRegister
) : Symbol<Float, FloatRegister>, ScriptValue.RegisterValue<Float> by mappedValue {
    override fun compile(context: CompileContext): CompiledScriptValue<ScriptValue<Float>> = renderSymbol(context)
}

// TODO this is maybe not needed
data class StringValue(val value: String) : ScriptValue<String> {
    override fun compile(context: CompileContext): CompiledScriptValue<StringValue> = render(value)
}

private fun <V : Any, SV : ScriptValue<V>> Symbol<V, SV>.renderSymbol(context: CompileContext): CompiledScriptValue<ScriptValue<V>> =
    if (context.options.minify) {
        mappedValue.compile(context)
    } else {
        render(name)
    }

private fun <V : Any, SV : ScriptValue<V>> SV.render(rendered: String): CompiledScriptValue<SV> =
    CompiledScriptValue(this, rendered)
