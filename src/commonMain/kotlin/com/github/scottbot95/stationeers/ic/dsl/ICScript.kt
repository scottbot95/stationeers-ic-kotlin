package com.github.scottbot95.stationeers.ic.dsl

class ICScript : SimpleScriptBlock() {

    override fun compile(options: CompileOptions): CompileResults {
        TODO("Not yet implemented")
    }
}

fun script(init: ICScript.() -> Unit): ICScript {
    val script = ICScript()
    script.init()
    return script
}
