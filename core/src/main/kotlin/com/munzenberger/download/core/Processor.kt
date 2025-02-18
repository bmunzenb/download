package com.munzenberger.download.core

import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.function.Consumer

data class ResultData(
    val bytes: Long,
)

val ResultFirst = Result.success(ResultData(-1))

class Processor(
    private val requestProperties: Map<String, String> = emptyMap(),
) {
    fun download(
        urlQueue: URLQueue,
        targetFactory: TargetFactory,
        callback: Consumer<ProcessorEvent> = LoggingProcessorEventConsumer(),
    ) {
        callback.accept(ProcessorEvent.QueueStarted(urlQueue))

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

            callback.accept(ProcessorEvent.DownloadResult(url, target, result))

            url = urlQueue.next(result)
        }

        callback.accept(ProcessorEvent.QueueCompleted(urlQueue))
    }

    private fun httpDownload(
        connection: HttpURLConnection,
        target: Target,
        callback: Consumer<ProcessorEvent>,
    ): Result<ResultData> {
        val url: URL = connection.url

        callback.accept(ProcessorEvent.DownloadStarted(url, target))

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
        callback: Consumer<ProcessorEvent>,
    ): Result<ResultData> {
        try {
            val bytes =
                target.write(
                    inStream = input,
                    progressCallback = { callback.accept(ProcessorEvent.DownloadProgress(url, target, it)) },
                )
            return Result.success(ResultData(bytes))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
