package com.munzenberger.download.images

import com.munzenberger.download.core.TargetFactory
import java.util.function.Consumer
import java.util.function.Function

class ImageDownloader(
    private val linkFilter: Function<String, Boolean>,
    private val imageFilter: Function<String, Boolean>,
    private val targetFactory: (String) -> TargetFactory,
    private val callback: Consumer<ImageDownloadStatus> = LoggingImageDownloadStatusConsumer(),
    private val processedRegistry: ProcessedRegistry = InMemoryProcessedRegistry(),
    private val depthFirst: Boolean = false,
) {
    private val jobQueue = mutableListOf<DownloadJob>()

    fun execute(url: String) {
        processedRegistry.addQueuedLink(QueuedUrl(url, url))
        jobQueue.add(linkJob(url, url))
        executeQueue()
    }

    fun resume() {
        processedRegistry.getQueuedLinks().forEach {
            jobQueue.add(linkJob(it.url, it.referer))
        }
        processedRegistry.getQueuedImages().forEach {
            jobQueue.add(
                ImagesJob(
                    images = listOf(it.url),
                    targetFactory = targetFactory.invoke(it.referer),
                    processedRegistry = processedRegistry,
                ),
            )
        }
        executeQueue()
    }

    @Suppress("TooGenericExceptionCaught")
    private fun executeQueue() {
        callback.accept(ImageDownloadStatus.StartProcessQueue)

        while (jobQueue.isNotEmpty()) {
            val job = if (depthFirst) jobQueue.removeLast() else jobQueue.removeFirst()
            try {
                callback.accept(ImageDownloadStatus.StartExecuteJob(job, jobQueue.size))
                job.run()
            } catch (e: Exception) {
                callback.accept(ImageDownloadStatus.Error(e))
            } finally {
                callback.accept(ImageDownloadStatus.EndExecuteJob(job, jobQueue.size))
            }
        }

        callback.accept(ImageDownloadStatus.EndProcessQueue)
    }

    private fun linkJob(
        url: String,
        referer: String,
    ) = LinkJob(
        url = url,
        referer = referer,
        processedRegistry = processedRegistry,
        linkFilter = linkFilter,
        imageFilter = imageFilter,
        addToQueue = { jobQueue.add(it) },
        targetFactory = targetFactory,
        callback = callback,
    )
}
