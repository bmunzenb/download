package com.munzenberger.download.images

interface ProcessedRegistry {
    fun addQueued(url: String)

    fun addProcessed(url: String)

    fun contains(url: String): Boolean
}

class InMemoryProcessedRegistry : ProcessedRegistry {
    private val queued = mutableSetOf<String>()
    private val processed = mutableSetOf<String>()

    override fun addQueued(url: String) {
        queued.add(url)
    }

    override fun addProcessed(url: String) {
        processed.add(url)
    }

    override fun contains(url: String): Boolean = queued.contains(url) || processed.contains(url)
}
