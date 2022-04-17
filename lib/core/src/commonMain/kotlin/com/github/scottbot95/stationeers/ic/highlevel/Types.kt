package com.github.scottbot95.stationeers.ic.highlevel

// TODO these names all collide with base kotlin types. Should we avoid that? Prefix all with IC?
abstract class Types {
    override fun toString(): String = this::class.simpleName ?: "<?>"

    abstract class Any : Types() {
        companion object : Any()
    }

    object Unit : Any() {
        fun toExpr() = 0.toExpr()
    }

    abstract class Number : Any() {
        companion object : Number()
    }

    object Int : Number()
    object Float : Number()
}
