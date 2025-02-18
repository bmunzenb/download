package com.munzenberger.download.core

import java.net.URI
import java.net.URL

fun interface URLQueue {
    fun next(result: Result<ResultData>): URL?

    companion object {
        fun of(vararg urls: String) = of(urls.toList())

        fun of(urls: Collection<String>) =
            object : URLQueue {
                private val i = urls.iterator()

                override fun next(result: Result<ResultData>) =
                    when {
                        i.hasNext() -> URI(i.next()).toURL()
                        else -> null
                    }

                override fun toString(): String = "${URLQueue::class.java.simpleName}(size=${urls.size})"
            }
    }

    operator fun plus(other: URLQueue): URLQueue = CompositeURLQueue(this, other)
}

private class CompositeURLQueue(
    private val left: URLQueue,
    private val right: URLQueue,
) : URLQueue {
    var isLeft = true

    override fun next(result: Result<ResultData>): URL? {
        val url =
            if (isLeft) {
                left.next(result)
            } else {
                right.next(result)
            }

        return if (url == null && isLeft) {
            isLeft = false
            next(ResultFirst)
        } else {
            url
        }
    }

    override fun toString(): String = "${this::class.simpleName}(left=$left, right=$right)"
}
