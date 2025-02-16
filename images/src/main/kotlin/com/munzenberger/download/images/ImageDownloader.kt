package com.munzenberger.download.images

import com.munzenberger.download.core.TargetFactory
import com.munzenberger.download.images.job.DownloadJob
import com.munzenberger.download.images.job.ImagesJob
import com.munzenberger.download.images.job.LinkJob
import com.munzenberger.download.images.registry.InMemoryURLRegistry
import com.munzenberger.download.images.registry.QueuedURL
import com.munzenberger.download.images.registry.URLRegistry
import java.util.function.Consumer
import java.util.function.Function

class ImageDownloader(
    private val linkFilter: Function<String, Boolean>,
    private val imageFilter: Function<String, Boolean>,
    private val targetFactory: (String) -> TargetFactory,
    private val callback: Consumer<ImageDownloadStatus> = LoggingImageDownloadStatusConsumer(),
    private val urlRegistry: URLRegistry = InMemoryURLRegistry(),
    private val depthFirst: Boolean = false,
) {
    private val jobQueue = mutableListOf<DownloadJob>()

    fun execute(url: String) {
        urlRegistry.addQueuedLinks(listOf(QueuedURL(url, url)))
        jobQueue.add(linkJob(url, url))
        executeQueue()
    }

    fun resume() {
        urlRegistry.getQueuedLinks().forEach {
            jobQueue.add(linkJob(it.url, it.referer))
        }
        urlRegistry.getQueuedImages().forEach {
            jobQueue.add(
                ImagesJob(
                    images = listOf(it.url),
                    targetFactory = targetFactory.invoke(it.referer),
                    urlRegistry = urlRegistry,
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
        urlRegistry = urlRegistry,
        linkFilter = linkFilter,
        imageFilter = imageFilter,
        addToQueue = { jobQueue.add(it) },
        targetFactory = targetFactory,
        callback = callback,
    )
}
