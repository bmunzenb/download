package com.munzenberger.download.images

interface ProcessedRegistry {
    fun addQueuedLink(url: String)

    fun addProcessedLink(url: String)

    fun addQueuedImage(url: String)

    fun addProcessedImage(url: String)

    fun contains(url: String): Boolean
}

class InMemoryProcessedRegistry : ProcessedRegistry {
    private val processed = mutableSetOf<String>()

    override fun addQueuedLink(url: String) {
        processed.add(url)
    }

    override fun addProcessedLink(url: String) {
        processed.add(url)
    }

    override fun addQueuedImage(url: String) {
        processed.add(url)
    }

    override fun addProcessedImage(url: String) {
        processed.add(url)
    }

    override fun contains(url: String): Boolean = processed.contains(url)
}
