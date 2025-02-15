package com.munzenberger.download.images

import java.util.function.Consumer

interface ImageDownloadStatusConsumer : Consumer<ImageDownloadStatus> {
    override fun accept(status: ImageDownloadStatus) {
        when (status) {
            ImageDownloadStatus.StartProcessQueue -> onStartProcessQueue()
            is ImageDownloadStatus.StartExecuteJob -> onStartExecuteJob(status.job, status.queueSize)
            is ImageDownloadStatus.StartProcessLink -> onStartProcessLink(status.url)
            is ImageDownloadStatus.EndProcessLink -> onEndProcessLink(status.url, status.images, status.links)
            is ImageDownloadStatus.EndExecuteJob -> onEndExecuteJob(status.job, status.queueSize)
            ImageDownloadStatus.EndProcessQueue -> onEndProcessQueue()
            is ImageDownloadStatus.Error -> onError(status.error)
        }
    }

    fun onStartProcessQueue() {}

    fun onStartExecuteJob(
        job: DownloadJob,
        queueSize: Int,
    ) {}

    fun onStartProcessLink(url: String) {}

    fun onEndProcessLink(
        url: String,
        images: Int,
        links: Int,
    ) {}

    fun onEndExecuteJob(
        job: DownloadJob,
        queueSize: Int,
    ) {}

    fun onEndProcessQueue() {}

    fun onError(error: Exception) {}
}
