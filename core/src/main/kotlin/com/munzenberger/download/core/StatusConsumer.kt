package com.munzenberger.download.core

import java.net.URL
import java.util.function.Consumer

interface StatusConsumer : Consumer<Status> {

    override fun accept(status: Status) {
        when (status) {
            is Status.QueueStarted -> onQueueStarted(status.queue)
            is Status.DownloadStarted -> onDownloadStarted(status.url, status.target)
            is Status.DownloadProgress -> onDownloadProgress(status.url, status.target, status.bytes)
            is Status.DownloadResult -> onDownloadResult(status.url, status.target, status.result)
            is Status.QueueCompleted -> onQueueCompleted(status.queue)
        }
    }

    fun onQueueStarted(queue: URLQueue) {}

    fun onDownloadStarted(url: URL, target: Target) {}

    fun onDownloadProgress(url: URL, target: Target, bytes: Long) {}

    fun onDownloadResult(url: URL, target: Target, result: Result) {}

    fun onQueueCompleted(queue: URLQueue) {}
}
