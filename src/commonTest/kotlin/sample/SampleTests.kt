package sample

import kotlin.test.Test
import kotlin.test.assertTrue

class SampleTests {
    fun testMe() {
        assertTrue(Sample().checkMe() > 0)
    }
}