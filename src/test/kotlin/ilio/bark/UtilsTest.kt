package ilio.bark

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify

internal class UtilsTest : TestBase({

    it("test wrapUnit") {
        (wrapUnit { }) shouldBe Unit
        (wrapUnit { 1 }) shouldBe Unit
        (wrapUnit { 1 + 2 }) shouldBe Unit
    }

    describe("test doIf") {
        val block = mockk<() -> Unit>()

        it("true.doIf") {
            true.doIf(block) shouldBe true
            verify(exactly = 1) { block() }
        }

        it("false.doIf") {
            false.doIf(block) shouldBe false
            verify(exactly = 0) { block() }
        }
    }

    describe("test doElse") {
        val block = mockk<() -> Unit>()

        it("true.doElse") {
            true.doElse(block) shouldBe true
            verify(exactly = 0) { block() }
        }

        it("false.doElse") {
            false.doElse(block) shouldBe false
            verify(exactly = 1) { block() }
        }
    }

})
