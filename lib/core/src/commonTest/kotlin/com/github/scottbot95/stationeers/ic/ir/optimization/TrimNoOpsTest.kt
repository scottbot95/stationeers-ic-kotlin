package com.github.scottbot95.stationeers.ic.ir.optimization

import com.github.scottbot95.stationeers.ic.ir.IRCompilation
import com.github.scottbot95.stationeers.ic.ir.IRCompileContext
import com.github.scottbot95.stationeers.ic.ir.IRRegister
import com.github.scottbot95.stationeers.ic.ir.IRStatement
import com.github.scottbot95.stationeers.ic.ir.makeReg
import com.github.scottbot95.stationeers.ic.ir.plusAssign
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class TrimNoOpsTest : WordSpec({
    val optimizer = IROptimizer(listOf(TrimNoOps))
    "TrimNoOps" should {
        "safely handle infinite Nop loops" {
            val start = IRStatement.Placeholder()
            val context = IRCompileContext(next = start::next)
            val nopStart = IRStatement.Nop()
            context += nopStart
            context += IRStatement.Copy(IRRegister(1U), IRRegister(0U))
            context += IRStatement.Copy(IRRegister(0U), IRRegister(0U)).apply {
                next = nopStart
            }

            val compilation = IRCompilation(mapOf(), start)

            optimizer.optimize(compilation)

            compilation.joinToString("\n") shouldBe "start:\nnop"
        }

        "delete dead writes" {
            val start = IRStatement.Placeholder()
            val context = IRCompileContext(next = start::next)
            context += IRStatement.Init(context.makeReg(), null, 0)
            context += IRStatement.Init(context.makeReg(), null, 1)
            context += IRStatement.Copy(context.makeReg(), IRRegister(1U))

            val compilation = IRCompilation(mapOf(), start)

            optimizer.optimize(compilation)

            compilation.joinToString("\n") shouldBe "start:\nnop"
        }
    }
})
