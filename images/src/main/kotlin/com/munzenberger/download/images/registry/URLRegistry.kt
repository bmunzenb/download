package com.munzenberger.download.images.registry

interface URLRegistry {
    fun addQueuedLinks(queuedUrls: Collection<QueuedURL>)

    fun addProcessedLink(url: String)

    fun addQueuedImages(queuedUrls: Collection<QueuedURL>)

    fun addProcessedImage(url: String)

    fun getQueuedLinks(): Collection<QueuedURL>

    fun getQueuedImages(): Collection<QueuedURL>

    fun contains(url: String): Boolean
}
