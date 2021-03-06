package com.munzenberger.download.core

import java.net.URL

interface URLQueue {
    fun next(result: Result) : URL?

    companion object {

        fun of(vararg urls: String) = object : URLQueue {
            private val i = urls.iterator()
            override fun next(result: Result) = when {
                    i.hasNext() -> URL(i.next())
                    else -> null
                }
        }

        fun of(urls: List<String>) = object : URLQueue {
            private val i = urls.iterator()
            override fun next(result: Result) = when {
                i.hasNext() -> URL(i.next())
                else -> null
            }
        }
    }
}

class TemplateURLQueue(private val urlTemplate: String, private val paramIterator: ParamIterator) : URLQueue {

    interface ParamIterator {
        fun next(result: Result): Array<out Any>?
    }

    override fun next(result: Result): URL? =
        when (val params = paramIterator.next(result)) {
            null -> null
            else -> URL(String.format(urlTemplate, *params))
    }
}

class IncrementUntilErrorParamIterator(start: Int) : TemplateURLQueue.ParamIterator {

    private var value = start

    override fun next(result: Result): Array<Any>? =
        when (result) {
            Result.SUCCESS -> {
                val p: Array<Any> = arrayOf(value)
                value++
                p
            }
            else -> null
    }
}

class RangeParamIterator(range: IntRange) : TemplateURLQueue.ParamIterator {

    private var value = range.iterator()

    override fun next(result: Result): Array<Any>? =
        when (value.hasNext()) {
            true -> arrayOf(value.nextInt())
            else -> null
    }
}

class ListParamIterator(params: List<Array<out Any>>) : TemplateURLQueue.ParamIterator {

    private val iterator = params.iterator()

    override fun next(result: Result): Array<out Any>? =
            when (iterator.hasNext()) {
                true -> iterator.next()
                else -> null
            }
}
