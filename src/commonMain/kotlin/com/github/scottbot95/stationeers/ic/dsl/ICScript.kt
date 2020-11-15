package com.github.scottbot95.stationeers.ic.dsl

class ICScript : SimpleScriptBlock()

fun script(init: ICScript.() -> Unit): ICScript {
    val script = ICScript()
    script.init()
    return script
}
