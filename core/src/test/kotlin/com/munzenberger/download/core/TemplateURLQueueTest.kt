package com.munzenberger.download.core

import org.junit.Assert.*
import org.junit.Test

class TemplateURLQueueTest {

    @Test
    fun `queue generates urls from template`() {

        val paramIterator = ListParamIterator(listOf(1, 'a'), listOf(2, 'b'), listOf(3, 'c'))

        val template = "http://example.com/%d/%s"

        val queue = TemplateURLQueue(template, paramIterator)

        assertEquals("http://example.com/1/a", queue.next(Result.First).toString())
        assertEquals("http://example.com/2/b", queue.next(Result.Success(0)).toString())
        assertEquals("http://example.com/3/c", queue.next(Result.Success(0)).toString())
        assertNull(queue.next(Result.Success(0)))
    }

    @Test
    fun `increment until error continues until error`() {

        val paramIterator = IncrementUntilErrorParamIterator(0)

        assertEquals(listOf(0), paramIterator.next(Result.First))
        assertEquals(listOf(1), paramIterator.next(Result.Success(0)))
        assertNull(paramIterator.next(Result.Error(0)))
    }
}
