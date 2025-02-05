package com.munzenberger.download.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TemplateURLQueueTest {
    private val success = Result.success(ResultData(0))
    private val failure = Result.failure<ResultData>(Exception())

    @Test
    fun `queue generates urls from template`() {
        val paramIterator = ListParamIterator(listOf(1, 'a'), listOf(2, 'b'), listOf(3, 'c'))

        val template = "http://example.com/%d/%s"

        val queue = TemplateURLQueue(template, paramIterator)

        assertEquals("http://example.com/1/a", queue.next(success).toString())
        assertEquals("http://example.com/2/b", queue.next(failure).toString())
        assertEquals("http://example.com/3/c", queue.next(success).toString())
        assertNull(queue.next(success))
    }

    @Test
    fun `increment until error continues until error`() {
        val paramIterator = IncrementUntilErrorParamIterator(0)

        assertEquals(listOf(0), paramIterator.next(success))
        assertEquals(listOf(1), paramIterator.next(success))
        assertNull(paramIterator.next(failure))
    }

    @Test
    fun `range iterates over range even when failure`() {
        val paramIterator = IntRangeParamIterator(1..3)

        assertEquals(listOf(1), paramIterator.next(success))
        assertEquals(listOf(2), paramIterator.next(failure))
        assertEquals(listOf(3), paramIterator.next(success))
        assertNull(paramIterator.next(success))
    }
}
