package com.munzenberger.download.core

import okio.buffer
import okio.sink
import okio.source
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class Status {
    data class QueueStarted(val queue: URLQueue) : Status()
    data class DownloadStarted(val url: URL, val target: Target) : Status()
    data class DownloadProgress(val url: URL, val target: Target, val bytes: Long) : Status()
    data class DownloadCompleted(val url: URL, val target: Target, val bytes: Long) : Status()
    data class DownloadFailed(val url: URL, val target: Target, val code: Int) : Status()
    data class DownloadError(val url: URL, val target: Target, val message: String) : Status()
    data class QueueCompleted(val queue: URLQueue) : Status()
}

typealias Callback = (Status) -> Unit

fun download(
    urlQueue: URLQueue,
    targetFactory: TargetFactory,
    callback: Callback = ConsoleLogger().callback,
    requestProperties: Map<String, String> = emptyMap()
) {

    callback.invoke(Status.QueueStarted(urlQueue))

    var result : Result = Result.Init

    var url = urlQueue.next(result)

    while (url != null) {

        val connection = url.openConnection()
        requestProperties.forEach { (k, v) ->
            connection.setRequestProperty(k, v)
        }

        val target = targetFactory.targetFor(url)

        result = when (connection) {
            is HttpURLConnection -> {
                httpDownload(connection, target, callback)
            }
            else -> {
                callback.invoke(Status.DownloadError(
                    url,
                    target,
                    "Unsupported connection type: ${connection::class.java.simpleName}"
                ))
                Result.SourceNotSupported
            }
        }

        url = urlQueue.next(result)
    }

    callback.invoke(Status.QueueCompleted(urlQueue))
}

private fun httpDownload(connection: HttpURLConnection, target: Target, callback: Callback) : Result {

    val url: URL = connection.url

    callback.invoke(Status.DownloadStarted(url, target))

    return when (val code = connection.responseCode) {
        HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_PARTIAL -> {
            val bytes = transfer(url, connection.inputStream, target, callback)
            callback.invoke(Status.DownloadCompleted(url, target, bytes))
            Result.Success(target)
        }
        else -> {
            callback.invoke(Status.DownloadFailed(url, target, code))
            Result.Error(code)
        }
    }
}

private fun transfer(url: URL, input: InputStream, target: Target, callback: Callback): Long {

    val source = input.source().buffer()
    val sink = target.open().sink().buffer()

    return source.use { inSource ->
        sink.use { outSink ->

            var total: Long = 0
            val byteArray = ByteArray(8192)
            var b = inSource.read(byteArray, 0, byteArray.size)

            while (b > 0) {
                outSink.write(byteArray, 0, b)
                total += b.toLong()
                callback.invoke(Status.DownloadProgress(url, target, total))
                b = inSource.read(byteArray, 0, byteArray.size)
            }

            total
        }
    }
}
