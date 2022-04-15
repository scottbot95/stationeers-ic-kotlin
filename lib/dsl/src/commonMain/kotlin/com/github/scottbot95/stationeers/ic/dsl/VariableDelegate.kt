package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.highlevel.Expression
import com.github.scottbot95.stationeers.ic.highlevel.NumberType
import com.github.scottbot95.stationeers.ic.highlevel.defVar
import kotlin.reflect.KProperty

class VariableDelegate(
    private val scope: ICScriptBlockScope,
    private val ident: Expression.Ident,
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Expression = ident

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Expression) {
        scope.context.currentScope?.apply {
            +Expression.Copy(value, ident)
        }
    }
}

class VariableDelegateProvider(
    private val scope: ICScriptBlockScope,
    private val name: String?,
    private val varType: NumberType
) {
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): VariableDelegate {
        val varName = name ?: property.name
        val ident = scope.context.scriptContext.defVar(varName, varType)
        return VariableDelegate(scope, Expression.Ident(ident))
    }
}
