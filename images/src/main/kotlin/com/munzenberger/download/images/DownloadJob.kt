package com.munzenberger.download.images

import com.munzenberger.download.core.LoggingStatusConsumer
import com.munzenberger.download.core.Processor
import com.munzenberger.download.core.ResultData
import com.munzenberger.download.core.StatusConsumer
import com.munzenberger.download.core.Target
import com.munzenberger.download.core.TargetFactory
import com.munzenberger.download.core.URLQueue
import org.jsoup.Jsoup
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.util.function.Consumer
import java.util.function.Function

fun interface DownloadJob : Runnable

@Suppress("LongParameterList")
internal class LinkJob(
    private val url: String,
    private val referer: String,
    private val processedRegistry: ProcessedRegistry,
    private val linkFilter: Function<String, Boolean>,
    private val imageFilter: Function<String, Boolean>,
    private val addToQueue: (DownloadJob) -> Unit,
    private val targetFactory: (String) -> TargetFactory,
    private val callback: Consumer<ImageDownloadStatus>,
) : DownloadJob {
    private data class Stats(
        val images: Int = 0,
        val links: Int = 0,
    )

    override fun run() {
        callback.accept(ImageDownloadStatus.StartProcessLink(url))

        val connection = URI.create(url).toURL().openConnection()
        val contentType = connection.contentType
        val (images, links) =
            when {
                contentType != null && contentType.contains("image", ignoreCase = true) -> processAsImage(url, referer)
                else -> processAsHtml(connection.getInputStream(), url)
            }

        processedRegistry.addProcessedLink(url)
        callback.accept(ImageDownloadStatus.EndProcessLink(url, images, links))
    }

    private fun processAsHtml(
        inStream: InputStream,
        url: String,
    ): Stats {
        val doc = Jsoup.parse(inStream, "UTF-8", url)

        val images =
            doc
                .getElementsByTag("img")
                .stream()
                .map { it.attr("abs:src") }
                .filter { it.isNotBlank() }
                .filter { !processedRegistry.contains(it) }
                .filter { imageFilter.apply(it) }
                .toList()
                .toSet()

        if (images.isNotEmpty()) {
            images.forEach {
                processedRegistry.addQueuedImage(QueuedUrl(it, url))
            }
            addToQueue.invoke(
                ImagesJob(
                    images,
                    targetFactory.invoke(url),
                    processedRegistry,
                ),
            )
        }

        val links =
            doc
                .getElementsByTag("a")
                .stream()
                .map { it.attr("abs:href") }
                .filter { it.isNotBlank() }
                .filter { !processedRegistry.contains(it) }
                .filter { linkFilter.apply(it) || imageFilter.apply(it) }
                .toList()
                .toSet()

        links.forEach {
            processedRegistry.addQueuedLink(QueuedUrl(it, referer))
            addToQueue(
                LinkJob(
                    it,
                    url,
                    processedRegistry,
                    linkFilter,
                    imageFilter,
                    addToQueue,
                    targetFactory,
                    callback,
                ),
            )
        }

        return Stats(images.size, links.size)
    }

    private fun processAsImage(
        url: String,
        referer: String,
    ): Stats {
        if (imageFilter.apply(url)) {
            processedRegistry.addQueuedImage(QueuedUrl(url, referer))
            addToQueue.invoke(
                ImagesJob(
                    listOf(url),
                    targetFactory.invoke(referer),
                    processedRegistry,
                ),
            )
            return Stats(images = 1)
        } else {
            return Stats()
        }
    }
}

internal class ImagesJob(
    private val images: Collection<String>,
    private val targetFactory: TargetFactory,
    private val processedRegistry: ProcessedRegistry,
) : DownloadJob {
    override fun run() {
        val markAsProcessed =
            object : StatusConsumer {
                override fun onDownloadResult(
                    url: URL,
                    target: Target,
                    result: Result<ResultData>,
                ) {
                    processedRegistry.addProcessedImage(url.toExternalForm())
                }
            }

        Processor().download(
            URLQueue.of(images),
            targetFactory,
            LoggingStatusConsumer().andThen(markAsProcessed),
        )
    }
}
