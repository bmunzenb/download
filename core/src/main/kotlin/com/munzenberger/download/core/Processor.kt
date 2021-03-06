package com.munzenberger.download.core

import okio.buffer
import okio.sink
import okio.source
import java.io.InputStream
import java.net.HttpURLConnection

fun download(urlQueue: URLQueue, targetFactory: TargetFactory, requestProperties: Map<String, String> = emptyMap(), logger: Logger = ConsoleLogger()) {

    var result = Result.SUCCESS

    var url = urlQueue.next(result)

    while (url != null) {

        val connection = url.openConnection()
        requestProperties.forEach { (k, v) ->
            connection.setRequestProperty(k, v)
        }

        result = when (connection) {
            is HttpURLConnection -> {
                val target = targetFactory.targetFor(url)
                httpDownload(connection, target, logger)
            }
            else -> {
                logger.println("Unsupported connection type: ${connection::class.java.simpleName}")
                Result.SOURCE_NOT_SUPPORTED
            }
        }

        url = urlQueue.next(result)
    }
}

private fun httpDownload(connection: HttpURLConnection, target: Target, logger: Logger) : Result {

    logger.print("${connection.url} -> $target ... ")

    return when (val code = connection.responseCode) {
        HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_PARTIAL -> {
            val start = System.currentTimeMillis()
            val bytes = transfer(connection.inputStream, target)
            val elapsed = System.currentTimeMillis() - start
            logger.println("${formatBytes(bytes)} in ${formatElapsed(elapsed)}.")
            Result.SUCCESS
        }
        else -> {
            logger.println("error $code.")
            Result.SOURCE_ERROR
        }
    }
}

private fun transfer(input: InputStream, target: Target): Long {

    val source = input.source().buffer()
    val sink = target.open().sink().buffer()

    return source.use { inSource ->
        sink.use { outSink ->
            inSource.readAll(outSink)
        }
    }
}

private fun formatBytes(bytes: Long) : String {

    if (bytes < 1024) {
        return "$bytes bytes"
    }

    val kbytes = bytes / 1024f

    if (kbytes < 1024f) {
        return "$kbytes KB"
    }

    val mbytes = kbytes / 1024f

    return "$mbytes MB"
}

private fun formatElapsed(elapsed: Long) : String {

    val seconds = elapsed / 1000
    val minutes = seconds / 60

    if (minutes > 0) {
        return "${minutes}m ${seconds}s"
    }

    if (seconds > 0) {
        return "${seconds}s"
    }

    return "${elapsed}ms"
}
