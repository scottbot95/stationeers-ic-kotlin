package com.github.scottbot95.stationeers.ic.ir.optimization

import com.github.scottbot95.stationeers.ic.ir.IRCompilation
import com.github.scottbot95.stationeers.ic.ir.IRRegister
import com.github.scottbot95.stationeers.ic.ir.IRStatement
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class TrimNoOpsTest : WordSpec({
    val optimizer = IROptimizer()
    "TrimNoOps" should {
        "safely handle infinite Nop loops" {
            val nop1 = IRStatement.Nop()
            val nop2 = IRStatement.Copy(IRRegister(0U), IRRegister(0U))
            nop1.next = nop2
            nop2.next = nop1

            val compilation = IRCompilation(mapOf(), nop1)
            optimizer.optimize(compilation)

            compilation.joinToString("\n") shouldBe "start:\nnop"
        }
    }
})