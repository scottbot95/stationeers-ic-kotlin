package com.github.scottbot95.stationeers.ic.ir.optimization

import com.github.scottbot95.stationeers.ic.ir.IRCompilation
import com.github.scottbot95.stationeers.ic.ir.IRCompileContext
import com.github.scottbot95.stationeers.ic.ir.IRRegister
import com.github.scottbot95.stationeers.ic.ir.IRStatement
import com.github.scottbot95.stationeers.ic.ir.makeReg
import com.github.scottbot95.stationeers.ic.ir.plusAssign
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class DeduplicationTest : WordSpec({
    val optimizer = IROptimizer(listOf(Deduplication))
    "Deduplication" should {
        "merge duplicate networks of statements" {
            val x = IRRegister(0U)

            val entrypoint = IRStatement.Init(x, null, 0)
            val context = IRCompileContext(
                regCount = 1U,
                next = entrypoint::next
            )
            val y = context.makeReg()
            val z = context.makeReg()
            context += IRStatement.Init(y, null, 1)
            context += IRStatement.Copy(z, x)
            context += IRStatement.Add(x, x, y)
            context += IRStatement.IfNotZero(z).apply ifnz@{
                cond = IRStatement.Add(x, x, z).apply {
                    next = IRStatement.Copy(z, x).apply {
                        next = IRStatement.Add(x, x, y).apply {
                            next = this@ifnz
                        }
                    }
                }
            }
            context += IRStatement.Return(x)
            val compilation = IRCompilation(emptyMap(), entrypoint)
            optimizer.optimize(compilation)

            compilation.joinToString("\n") shouldBe """
                start:
                init R0 null 0
                init R1 null 1
                L1:
                copy R2 R0
                add R0 R0 R1
                ifnz R2, JMP L0
                ret R0
                L0:
                add R0 R0 R2
                jmp L1
            """.trimIndent()
        }
    }
})
