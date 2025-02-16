package com.munzenberger.download.images

data class QueuedUrl(
    val url: String,
    val referer: String,
)

interface ProcessedRegistry {
    fun addQueuedLink(queuedUrl: QueuedUrl)

    fun addProcessedLink(url: String)

    fun addQueuedImage(queuedUrl: QueuedUrl)

    fun addProcessedImage(url: String)

    fun getQueuedLinks(): Collection<QueuedUrl>

    fun getQueuedImages(): Collection<QueuedUrl>

    fun contains(url: String): Boolean
}

open class InMemoryProcessedRegistry : ProcessedRegistry {
    protected val queuedLinks = mutableMapOf<String, String>()
    protected val processedLinks = mutableSetOf<String>()
    protected val queuedImages = mutableMapOf<String, String>()
    protected val processedImages = mutableSetOf<String>()

    override fun addQueuedLink(queuedUrl: QueuedUrl) {
        queuedLinks[queuedUrl.url] = queuedUrl.referer
    }

    override fun addProcessedLink(url: String) {
        queuedLinks.remove(url)
        processedLinks.add(url)
    }

    override fun addQueuedImage(queuedUrl: QueuedUrl) {
        queuedImages[queuedUrl.url] = queuedUrl.referer
    }

    override fun addProcessedImage(url: String) {
        queuedImages.remove(url)
        processedImages.add(url)
    }

    override fun getQueuedLinks(): Collection<QueuedUrl> = queuedLinks.map { QueuedUrl(it.key, it.value) }

    override fun getQueuedImages(): Collection<QueuedUrl> = queuedImages.map { QueuedUrl(it.key, it.value) }

    @Suppress("MaxLineLength")
    override fun contains(url: String): Boolean =
        queuedLinks.keys.contains(url) || processedLinks.contains(url) || queuedImages.keys.contains(url) || processedImages.contains(url)
}
