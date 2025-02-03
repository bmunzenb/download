package com.munzenberger.download.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URI

class CompositeURLQueueTest {
    @Test
    fun `it composes multiple URL queues`() {
        val queue1 =
            URLQueue.of(
                "http://foo.com/1",
                "http://foo.com/2",
                "http://foo.com/3",
            )

        val queue2 =
            URLQueue.of(
                "http://bar.com/1",
                "http://bar.com/2",
            )

        val queue3 =
            URLQueue.of(
                "http://fizz.com/1",
            )

        val composite = queue1 + queue2 + queue3

        val urls =
            buildList {
                val result = Result.success(ResultData(0))
                var url = composite.next(result)
                while (url != null) {
                    add(url)
                    url = composite.next(result)
                }
            }

        val expected =
            listOf(
                URI("http://foo.com/1").toURL(),
                URI("http://foo.com/2").toURL(),
                URI("http://foo.com/3").toURL(),
                URI("http://bar.com/1").toURL(),
                URI("http://bar.com/2").toURL(),
                URI("http://fizz.com/1").toURL(),
            )

        assertEquals(expected, urls)
    }
}
