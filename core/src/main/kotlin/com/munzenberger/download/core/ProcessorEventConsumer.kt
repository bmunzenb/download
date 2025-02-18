package com.munzenberger.download.core

import java.net.URL
import java.util.function.Consumer

interface ProcessorEventConsumer : Consumer<ProcessorEvent> {
    override fun accept(event: ProcessorEvent) {
        when (event) {
            is ProcessorEvent.QueueStarted -> onQueueStarted(event.queue)
            is ProcessorEvent.DownloadStarted -> onDownloadStarted(event.url, event.target)
            is ProcessorEvent.DownloadProgress -> onDownloadProgress(event.url, event.target, event.bytes)
            is ProcessorEvent.DownloadResult -> onDownloadResult(event.url, event.target, event.result)
            is ProcessorEvent.QueueCompleted -> onQueueCompleted(event.queue)
        }
    }

    fun onQueueStarted(queue: URLQueue) {}

    fun onDownloadStarted(
        url: URL,
        target: Target,
    ) {}

    fun onDownloadProgress(
        url: URL,
        target: Target,
        bytes: Long,
    ) {}

    fun onDownloadResult(
        url: URL,
        target: Target,
        result: Result<ResultData>,
    ) {}

    fun onQueueCompleted(queue: URLQueue) {}
}
