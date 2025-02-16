package com.munzenberger.download.images.job

import com.munzenberger.download.core.LoggingStatusConsumer
import com.munzenberger.download.core.Processor
import com.munzenberger.download.core.ResultData
import com.munzenberger.download.core.StatusConsumer
import com.munzenberger.download.core.Target
import com.munzenberger.download.core.TargetFactory
import com.munzenberger.download.core.URLQueue
import com.munzenberger.download.images.registry.URLRegistry
import java.net.URL

internal class ImagesJob(
    private val images: Collection<String>,
    private val targetFactory: TargetFactory,
    private val urlRegistry: URLRegistry,
) : DownloadJob {
    override fun run() {
        val markAsProcessed =
            object : StatusConsumer {
                override fun onDownloadResult(
                    url: URL,
                    target: Target,
                    result: Result<ResultData>,
                ) {
                    urlRegistry.addProcessedImage(url.toExternalForm())
                }
            }

        Processor().download(
            URLQueue.of(images),
            targetFactory,
            LoggingStatusConsumer().andThen(markAsProcessed),
        )
    }
}
