package com.munzenberger.download.images.registry

class InMemoryURLRegistry(
    private val queuedLinks: MutableMap<String, String> = mutableMapOf(),
    private val processedLinks: MutableSet<String> = mutableSetOf(),
    private val queuedImages: MutableMap<String, String> = mutableMapOf(),
    private val processedImages: MutableSet<String> = mutableSetOf(),
) : URLRegistry {
    override fun addQueuedLinks(queuedUrls: Collection<QueuedURL>) {
        queuedUrls.forEach {
            queuedLinks[it.url] = it.referer
        }
    }

    override fun addProcessedLink(url: String) {
        queuedLinks.remove(url)
        processedLinks.add(url)
    }

    override fun addQueuedImages(queuedUrls: Collection<QueuedURL>) {
        queuedUrls.forEach {
            queuedImages[it.url] = it.referer
        }
    }

    override fun addProcessedImage(url: String) {
        queuedImages.remove(url)
        processedImages.add(url)
    }

    override fun getQueuedLinks(): Collection<QueuedURL> = queuedLinks.map { QueuedURL(it.key, it.value) }

    override fun getQueuedImages(): Collection<QueuedURL> = queuedImages.map { QueuedURL(it.key, it.value) }

    @Suppress("MaxLineLength")
    override fun contains(url: String): Boolean =
        queuedLinks.keys.contains(url) || processedLinks.contains(url) || queuedImages.keys.contains(url) || processedImages.contains(url)
}
