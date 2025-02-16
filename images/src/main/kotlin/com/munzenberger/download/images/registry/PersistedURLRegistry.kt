package com.munzenberger.download.images.registry

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

@Suppress("TooManyFunctions")
class PersistedURLRegistry(
    rootPath: Path,
) : URLRegistry {
    private val queuedLinks: MutableMap<String, String> = mutableMapOf()
    private val processedLinks: MutableSet<String> = mutableSetOf()
    private val queuedImages: MutableMap<String, String> = mutableMapOf()
    private val processedImages: MutableSet<String> = mutableSetOf()

    private val registry =
        InMemoryURLRegistry(
            queuedLinks,
            processedLinks,
            queuedImages,
            processedImages,
        )

    private val queuedLinksFile = rootPath.resolve("links.queued")
    private val processedLinksFile = rootPath.resolve("links.processed")
    private val queuedImagesFile = rootPath.resolve("images.queued")
    private val processedImagesFile = rootPath.resolve("images.processed")

    init {
        if (Files.exists(queuedLinksFile)) {
            readInto(queuedLinksFile, queuedLinks)
        } else {
            Files.createFile(queuedLinksFile)
        }

        if (Files.exists(processedLinksFile)) {
            readInto(processedLinksFile, processedLinks)
        } else {
            Files.createFile(processedLinksFile)
        }

        if (Files.exists(queuedImagesFile)) {
            readInto(queuedImagesFile, queuedImages)
        } else {
            Files.createFile(queuedImagesFile)
        }

        if (Files.exists(processedImagesFile)) {
            readInto(processedImagesFile, processedImages)
        } else {
            Files.createFile(processedImagesFile)
        }
    }

    private fun readInto(
        path: Path,
        queued: MutableMap<String, String>,
    ) {
        Files.readAllLines(path).forEach {
            val s = it.split(' ', limit = 2)
            queued[s[0]] = s[1]
        }
    }

    private fun readInto(
        path: Path,
        processed: MutableSet<String>,
    ) {
        Files.readAllLines(path).forEach {
            processed.add(it)
        }
    }

    private fun writeTo(
        queued: MutableMap<String, String>,
        path: Path,
    ) {
        val lines = queued.map { "${it.key} ${it.value}" }
        Files.write(path, lines, StandardOpenOption.TRUNCATE_EXISTING)
    }

    private fun writeTo(
        processed: MutableSet<String>,
        path: Path,
    ) {
        Files.write(path, processed, StandardOpenOption.TRUNCATE_EXISTING)
    }

    override fun addQueuedLinks(queuedUrls: Collection<QueuedURL>) {
        registry.addQueuedLinks(queuedUrls)
        writeTo(queuedLinks, queuedLinksFile)
    }

    override fun addProcessedLink(url: String) {
        registry.addProcessedLink(url)
        writeTo(queuedLinks, queuedLinksFile)
        writeTo(processedLinks, processedLinksFile)
    }

    override fun addQueuedImages(queuedUrls: Collection<QueuedURL>) {
        registry.addQueuedImages(queuedUrls)
        writeTo(queuedImages, queuedImagesFile)
    }

    override fun addProcessedImage(url: String) {
        registry.addProcessedImage(url)
        writeTo(queuedImages, queuedImagesFile)
        writeTo(processedImages, processedImagesFile)
    }

    override fun getQueuedLinks() = registry.getQueuedLinks()

    override fun getQueuedImages() = registry.getQueuedImages()

    override fun contains(url: String) = registry.contains(url)
}
