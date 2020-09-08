package com.munzenberger.download.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TemplateURLQueueTest {

    @Test
    fun `queue generates urls from template`() {

        val paramIterator = ListParamIterator(listOf(arrayOf(1, 'a'), arrayOf(2, 'b'), arrayOf(3, 'c')))

        val template = "http://example.com/%d/%s"

        val queue = TemplateURLQueue(template, paramIterator)

        assertEquals("http://example.com/1/a", queue.next(Result.SUCCESS).toString())
        assertEquals("http://example.com/2/b", queue.next(Result.SUCCESS).toString())
        assertEquals("http://example.com/3/c", queue.next(Result.SUCCESS).toString())
        assertNull(queue.next(Result.SUCCESS))
    }
}
