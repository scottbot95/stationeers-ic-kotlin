@JsExport
abstract class Foo

@JsExport
object FooObj : Foo()

@JsExport
fun doAThing() {
    console.log(FooObj::class.simpleName)
}

val dirtyHack = doAThing()
