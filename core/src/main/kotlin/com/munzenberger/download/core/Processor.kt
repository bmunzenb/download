package com.munzenberger.download.core

import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.function.Consumer

data class ResultData(
    val bytes: Long,
)

val ResultFirst = Result.success(ResultData(-1))

sealed class Status {
    data class QueueStarted(
        val queue: URLQueue,
    ) : Status()

    data class DownloadStarted(
        val url: URL,
        val target: Target,
    ) : Status()

    data class DownloadProgress(
        val url: URL,
        val target: Target,
        val bytes: Long,
    ) : Status()

    data class DownloadResult(
        val url: URL,
        val target: Target,
        val result: Result<ResultData>,
    ) : Status()

    data class QueueCompleted(
        val queue: URLQueue,
    ) : Status()
}

class Processor(
    private val requestProperties: Map<String, String> = emptyMap(),
) {
    fun download(
        urlQueue: URLQueue,
        targetFactory: TargetFactory,
        callback: Consumer<Status> = LoggingStatusConsumer(),
    ) {
        callback.accept(Status.QueueStarted(urlQueue))

        var url = urlQueue.next(ResultFirst)

        while (url != null) {
            val connection =
                url.openConnection().also { connection ->
                    requestProperties.forEach { (key, value) ->
                        connection.setRequestProperty(key, value)
                    }
                }

            val target = targetFactory.create(url)

            val result =
                when (connection) {
                    is HttpURLConnection ->
                        httpDownload(connection, target, callback)

                    else ->
                        transfer(url, connection.getInputStream(), target, callback)
                }

            callback.accept(Status.DownloadResult(url, target, result))

            url = urlQueue.next(result)
        }

        callback.accept(Status.QueueCompleted(urlQueue))
    }

    private fun httpDownload(
        connection: HttpURLConnection,
        target: Target,
        callback: Consumer<Status>,
    ): Result<ResultData> {
        val url: URL = connection.url

        callback.accept(Status.DownloadStarted(url, target))

        return when (val code = connection.responseCode) {
            HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_PARTIAL -> {
                return transfer(url, connection.inputStream, target, callback)
            }
            else -> {
                Result.failure(HttpException(code))
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun transfer(
        url: URL,
        input: InputStream,
        target: Target,
        callback: Consumer<Status>,
    ): Result<ResultData> {
        try {
            val bytes =
                target.write(
                    inStream = input,
                    progressCallback = { callback.accept(Status.DownloadProgress(url, target, it)) },
                )
            return Result.success(ResultData(bytes))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
