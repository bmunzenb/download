package com.munzenberger.download.core

import java.net.URI
import java.net.URL

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
