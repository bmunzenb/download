package com.munzenberger.download.images.job

import com.munzenberger.download.core.TargetFactory
import com.munzenberger.download.images.ImageDownloadStatus
import com.munzenberger.download.images.registry.QueuedURL
import com.munzenberger.download.images.registry.URLRegistry
import org.jsoup.Jsoup
import java.io.InputStream
import java.net.URI
import java.util.function.Consumer
import java.util.function.Function

@Suppress("LongParameterList")
internal class LinkJob(
    private val url: String,
    private val referer: String,
    private val urlRegistry: URLRegistry,
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

        urlRegistry.addProcessedLink(url)
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
                .filter { !urlRegistry.contains(it) }
                .filter { imageFilter.apply(it) }
                .toList()
                .toSet()

        if (images.isNotEmpty()) {
            urlRegistry.addQueuedImages(images.map { QueuedURL(it, url) })
            addToQueue.invoke(
                ImagesJob(
                    images,
                    targetFactory.invoke(url),
                    urlRegistry,
                ),
            )
        }

        val links =
            doc
                .getElementsByTag("a")
                .stream()
                .filter { !it.attr("href").startsWith("#") }
                .map { it.attr("abs:href") }
                .filter { it.isNotBlank() }
                .filter { !urlRegistry.contains(it) }
                .filter { linkFilter.apply(it) || imageFilter.apply(it) }
                .toList()
                .toSet()

        if (links.isNotEmpty()) {
            urlRegistry.addQueuedLinks(links.map { QueuedURL(it, url) })
            links.forEach {
                addToQueue(
                    LinkJob(
                        it,
                        url,
                        urlRegistry,
                        linkFilter,
                        imageFilter,
                        addToQueue,
                        targetFactory,
                        callback,
                    ),
                )
            }
        }

        return Stats(images.size, links.size)
    }

    private fun processAsImage(
        url: String,
        referer: String,
    ): Stats {
        if (imageFilter.apply(url)) {
            urlRegistry.addQueuedImages(listOf(QueuedURL(url, referer)))
            addToQueue.invoke(
                ImagesJob(
                    listOf(url),
                    targetFactory.invoke(referer),
                    urlRegistry,
                ),
            )
            return Stats(images = 1)
        } else {
            return Stats()
        }
    }
}
