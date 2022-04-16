package com.github.scottbot95.stationeers.ic.dsl

import com.github.scottbot95.stationeers.ic.highlevel.Expression
import com.github.scottbot95.stationeers.ic.highlevel.Types
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
    private val varType: Types.Any,
    private val defaultValue: Expression?,
) {
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): VariableDelegate {
        val varName = name ?: property.name
        val ident = scope.context.scriptContext.defVar(varName, varType)
        val delegate = VariableDelegate(scope, ident)
        if (defaultValue != null) {
            delegate.setValue(null, property, defaultValue)
        }
        return delegate
    }
}
