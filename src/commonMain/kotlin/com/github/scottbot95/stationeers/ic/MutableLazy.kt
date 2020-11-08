package com.github.scottbot95.stationeers.ic

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> mutableLazy(initializer: () -> T?): ReadWriteProperty<Any?, T> =
    object : ReadWriteProperty<Any?, T> {
        private var initialized: Boolean = false

        private var value: T? = null

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            this.value = value
            initialized = true
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (!initialized) {
                value = initializer()
                initialized = true
            }
            @Suppress("UNCHECKED_CAST")
            return value as T
        }

    }
