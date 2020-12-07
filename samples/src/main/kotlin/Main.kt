import com.github.scottbot95.stationeers.ic.dsl.CompileOptions
import com.github.scottbot95.stationeers.ic.dsl.compile
import kotlin.math.ceil
import kotlin.math.floor

fun main() {
    val compileOptions = CompileOptions()

    mapOf(
        "Electric Furnace Control" to electricFurnaceControl
    ).forEach {
        val headerSize = (50.0 - it.key.length) / 2.0
        println("*".repeat(floor(headerSize).toInt()) + it.key + "*".repeat(ceil(headerSize).toInt()))
        println(it.value.compile(compileOptions).toString())
    }
}
