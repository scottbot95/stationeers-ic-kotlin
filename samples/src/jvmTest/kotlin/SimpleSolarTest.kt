import com.github.scottbot95.stationeers.ic.CompileOptions
import com.github.scottbot95.stationeers.ic.ICScriptBuilder
import com.github.scottbot95.stationeers.ic.writeToString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SimpleSolarTest {

    @Test
    fun testDefaultOptions() {
        val options = CompileOptions()
        val script = ICScriptBuilder.standard()
            .apply(ICScriptBuilder::simpleSolarTracking)
            .compile(options)

        val scriptString = script.writeToString()

        Assertions.assertEquals("""
            l r0 d1 PrefabHash
            l r1 d0 Vertical
            sub r3 r1 15
            div r2 r3 1.5
            sb r0 Vertical r2
            yield
            j 0
        """.trimIndent(), scriptString)
    }
}