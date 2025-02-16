package com.munzenberger.download.images

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class FlatFileProcessedRegistry(
    rootPath: Path,
) : InMemoryProcessedRegistry() {
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

    override fun addQueuedLink(queuedUrl: QueuedUrl) {
        super.addQueuedLink(queuedUrl)
        writeTo(queuedLinks, queuedLinksFile)
    }

    override fun addProcessedLink(url: String) {
        super.addProcessedLink(url)
        writeTo(queuedLinks, queuedLinksFile)
        writeTo(processedLinks, processedLinksFile)
    }

    override fun addQueuedImage(queuedUrl: QueuedUrl) {
        super.addQueuedImage(queuedUrl)
        writeTo(queuedImages, queuedImagesFile)
    }

    override fun addProcessedImage(url: String) {
        super.addProcessedImage(url)
        writeTo(queuedImages, queuedImagesFile)
        writeTo(processedImages, processedImagesFile)
    }
}
