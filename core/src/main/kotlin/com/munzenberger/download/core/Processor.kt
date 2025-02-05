package com.munzenberger.download.core

import okio.buffer
import okio.sink
import okio.source
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
    private val bufferSize: Int = DEFAULT_BUFFER_SIZE,
) {
    companion object {
        const val DEFAULT_BUFFER_SIZE = 8192
    }

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
                        Result.failure(SourceNotSupportedException(url))
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
                val data = transfer(url, connection.inputStream, target, callback)
                Result.success(data)
            }

            else -> {
                Result.failure(HttpException(code))
            }
        }
    }

    private fun transfer(
        url: URL,
        input: InputStream,
        target: Target,
        callback: Consumer<Status>,
    ): ResultData {
        val source = input.source().buffer()
        val sink = target.open().sink().buffer()

        return source.use { inSource ->
            sink.use { outSink ->

                var total: Long = 0
                val byteArray = ByteArray(bufferSize)
                var b = inSource.read(byteArray, 0, byteArray.size)

                while (b > 0) {
                    outSink.write(byteArray, 0, b)
                    total += b.toLong()
                    callback.accept(Status.DownloadProgress(url, target, total))
                    b = inSource.read(byteArray, 0, byteArray.size)
                }

                ResultData(total)
            }
        }
    }
}
