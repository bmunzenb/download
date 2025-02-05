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
            next(result)
        } else {
            url
        }
    }

    override fun toString(): String = "${this::class.simpleName}(left=$left, right=$right)"
}

class TemplateURLQueue(
    private val urlTemplate: String,
    private val paramIterator: ParamIterator,
) : URLQueue {
    interface ParamIterator {
        fun next(result: Result<ResultData>): List<Any>?
    }

    @Suppress("SpreadOperator")
    override fun next(result: Result<ResultData>): URL? =
        when (val params = paramIterator.next(result)) {
            null -> null
            else -> URI(String.format(urlTemplate, *params.toTypedArray())).toURL()
        }

    @Suppress("MaxLineLength")
    override fun toString(): String = "${this::class.simpleName}(template=$urlTemplate, paramIterator=$paramIterator)"
}

class IncrementUntilErrorParamIterator(
    private val start: Int,
) : TemplateURLQueue.ParamIterator {
    private var value = start

    override fun next(result: Result<ResultData>): List<Any>? =
        when {
            result.isSuccess -> {
                val p: List<Any> = listOf(value)
                value++
                p
            }
            else -> null
        }

    override fun toString(): String = "${this::class.simpleName}(start=$start)"
}

class IntRangeParamIterator(
    private val range: IntRange,
) : TemplateURLQueue.ParamIterator {
    private var value = range.iterator()

    override fun next(result: Result<ResultData>): List<Any>? =
        when (value.hasNext()) {
            true -> listOf(value.nextInt())
            else -> null
        }

    override fun toString(): String = "${this::class.simpleName}(range=$range)"
}

class ListParamIterator(
    vararg params: List<Any>,
) : TemplateURLQueue.ParamIterator {
    private val size = params.size
    private val iterator = params.iterator()

    override fun next(result: Result<ResultData>): List<Any>? =
        when (iterator.hasNext()) {
            true -> iterator.next()
            else -> null
        }

    override fun toString(): String = "${this::class.simpleName}(size=$size)"
}
