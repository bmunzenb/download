package com.munzenberger.download.images

import com.munzenberger.download.core.formatElapsed
import java.util.Locale

class LoggingImageDownloadStatusConsumer(
    private val locale: Locale = Locale.getDefault(),
) : ImageDownloadStatusConsumer {
    // statistics
    private var queueStart: Long = 0
    private var jobCount: Int = 0

    override fun onStartProcessQueue() {
        queueStart = System.currentTimeMillis()
    }

    override fun onStartExecuteJob(
        job: DownloadJob,
        queueSize: Int,
    ) {
        jobCount++
        println(
            String.format(
                locale,
                "[%,d] Executing job from queue, %,d %s remaining...",
                jobCount,
                queueSize,
                "job".pluralize(queueSize),
            ),
        )
    }

    override fun onStartProcessLink(url: String) {
        print("Processing $url ...")
    }

    override fun onEndProcessLink(
        url: String,
        images: Int,
        links: Int,
    ) {
        val str =
            when {
                images > 0 && links > 0 ->
                    String.format(
                        locale,
                        " added %,d %s and %,d %s to queue.",
                        images,
                        "image".pluralize(images),
                        links,
                        "link".pluralize(links),
                    )
                images > 0 -> {
                    String.format(
                        locale,
                        " added %,d %s to queue.",
                        images,
                        "image".pluralize(images),
                    )
                }
                links > 0 -> {
                    String.format(
                        locale,
                        " added %,d %s to queue.",
                        links,
                        "link".pluralize(links),
                    )
                }
                else -> {
                    " done."
                }
            }
        println(str)
    }

    override fun onEndProcessQueue() {
        val elapsed = System.currentTimeMillis() - queueStart
        println(
            String.format(
                locale,
                "Executed %,d %s in %s.",
                jobCount,
                "job".pluralize(jobCount),
                elapsed.formatElapsed,
            ),
        )
    }

    override fun onError(error: Exception) {
        error.printStackTrace(System.err)
    }
}

private fun String.pluralize(count: Int): String =
    when {
        count == 1 -> this
        else -> "${this}s"
    }
