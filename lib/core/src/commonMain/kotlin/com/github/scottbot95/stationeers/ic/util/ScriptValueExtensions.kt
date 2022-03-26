package com.github.scottbot95.stationeers.ic.util

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.DeviceLiteral
import com.github.scottbot95.stationeers.ic.FloatLiteral
import com.github.scottbot95.stationeers.ic.FloatRegister
import com.github.scottbot95.stationeers.ic.IntLiteral
import com.github.scottbot95.stationeers.ic.IntRegister
import com.github.scottbot95.stationeers.ic.NumberLiteral
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.StringLiteral

fun NumberLiteral<*>.toInt(): Int = value.toInt()
fun NumberLiteral<*>.toFloat(): Float = value.toFloat()

//region ScriptValue Converters

fun Int.toScriptValue(): IntLiteral = IntLiteral(this)
fun Float.toScriptValue(): FloatLiteral = FloatLiteral(this)
fun Double.toScriptValue(): FloatLiteral = FloatLiteral(this.toFloat())
fun String.toScriptValue(): StringLiteral = StringLiteral(this)
fun Device.toScriptValue(): DeviceLiteral = DeviceLiteral(this)
fun Register.asIntRegister(): IntRegister = IntRegister(this)
fun Register.asFloatRegister(): FloatRegister = FloatRegister(this)

//endregion