package com.github.scottbot95.stationeers.ic

sealed class Device(val index: Int, val token: String = "d$index") {
    object D0 : Device(0)
    object D1 : Device(1)
    object D2 : Device(2)
    object D3 : Device(3)
    object D4 : Device(4)
    object D5 : Device(5)
    object DB : Device(6, "db")

    companion object {
        private val devices = arrayOf(
            D0, D1, D2, D3, D4, D5, DB
        )

        fun getByIndex(index: Int): Device = devices[index]
    }
}
