package com.munzenberger.download.core

import java.net.URI
import java.net.URL

interface URLQueue {
    fun next(result: Result) : URL?

    companion object {

        fun of(vararg urls: String) = of(urls.toList())

        fun of(urls: Collection<String>) = object : URLQueue {

            private val i = urls.iterator()

            override fun next(result: Result) = when {
                i.hasNext() -> URI(i.next()).toURL()
                else -> null
            }

            override fun toString(): String {
                return "${URLQueue::class.java.name}(size=${urls.size})"
            }
        }
    }
}

class TemplateURLQueue(private val urlTemplate: String, private val paramIterator: ParamIterator) : URLQueue {

    interface ParamIterator {
        fun next(result: Result): List<Any>?
    }

    @Suppress("SpreadOperator")
    override fun next(result: Result): URL? =
        when (val params = paramIterator.next(result)) {
            null -> null
            else -> URI(String.format(urlTemplate, *params.toTypedArray())).toURL()
    }

    override fun toString(): String {
        return "${this::class.java.name}(urlTemplate=$urlTemplate, paramIterator=${paramIterator::class.java.name})"
    }
}

class IncrementUntilErrorParamIterator(start: Int) : TemplateURLQueue.ParamIterator {

    private var value = start

    override fun next(result: Result): List<Any>? =
        when (result) {
            is Result.First, is Result.Success -> {
                val p: List<Any> = listOf(value)
                value++
                p
            }
            else -> null
    }
}

class IntRangeParamIterator(range: IntRange) : TemplateURLQueue.ParamIterator {

    private var value = range.iterator()

    override fun next(result: Result): List<Any>? =
        when (value.hasNext()) {
            true -> listOf(value.nextInt())
            else -> null
    }
}

class ListParamIterator(vararg params: List<Any>) : TemplateURLQueue.ParamIterator {

    private val iterator = params.iterator()

    override fun next(result: Result): List<Any>? =
            when (iterator.hasNext()) {
                true -> iterator.next()
                else -> null
            }
}
