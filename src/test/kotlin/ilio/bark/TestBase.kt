package ilio.bark

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.mockk.clearAllMocks

abstract class TestBase(body: DescribeSpec.() -> Unit = {}) : DescribeSpec(body) {
    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        clearAllMocks()
    }
}
