package com.munzenberger.download.core

import java.net.URL

sealed class ProcessorEvent {
    data class QueueStarted(
        val queue: URLQueue,
    ) : ProcessorEvent()

    data class DownloadStarted(
        val url: URL,
        val target: Target,
    ) : ProcessorEvent()

    data class DownloadProgress(
        val url: URL,
        val target: Target,
        val bytes: Long,
    ) : ProcessorEvent()

    data class DownloadResult(
        val url: URL,
        val target: Target,
        val result: Result<ResultData>,
    ) : ProcessorEvent()

    data class QueueCompleted(
        val queue: URLQueue,
    ) : ProcessorEvent()
}
