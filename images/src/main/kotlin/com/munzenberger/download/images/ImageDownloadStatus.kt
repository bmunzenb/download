package com.munzenberger.download.images

import com.munzenberger.download.images.job.DownloadJob

sealed class ImageDownloadStatus {
    data object StartProcessQueue : ImageDownloadStatus()

    data class StartExecuteJob(
        val job: DownloadJob,
        val queueSize: Int,
    ) : ImageDownloadStatus()

    data class StartProcessLink(
        val url: String,
    ) : ImageDownloadStatus()

    data class EndProcessLink(
        val url: String,
        val images: Int,
        val links: Int,
    ) : ImageDownloadStatus()

    data class EndExecuteJob(
        val job: DownloadJob,
        val queueSize: Int,
    ) : ImageDownloadStatus()

    data object EndProcessQueue : ImageDownloadStatus()

    data class Error(
        val error: Exception,
    ) : ImageDownloadStatus()
}
