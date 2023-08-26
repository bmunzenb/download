package com.munzenberger.download.core

import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection

fun download(
    urlQueue: URLQueue,
    targetFactory: TargetFactory,
    requestProperties: Map<String, String> = emptyMap(),
    postProcess: (Target) -> Unit = {},
    logger: Logger = ConsoleLogger()
) {

    var result : Result = Result.Success(-1, -1)

    var url = urlQueue.next(result)

    var totalItems = 0;
    var totalBytes = 0L;
    var elapsed = System.currentTimeMillis();

    while (url != null) {

        val connection = url.openConnection()
        requestProperties.forEach { (k, v) ->
            connection.setRequestProperty(k, v)
        }

        val target = targetFactory.targetFor(url)

        result = when (connection) {
            is HttpURLConnection -> {
                httpDownload(connection, target, logger)
            }
            else -> {
                logger.println("Unsupported connection type: ${connection::class.java.simpleName}")
                Result.SourceNotSupported
            }
        }

        if (result is Result.Success) {
            totalItems++
            totalBytes += result.bytes
            postProcess.invoke(target)
        }

        url = urlQueue.next(result)
    }

    elapsed = System.currentTimeMillis() - elapsed;

    val str = String.format("%,d", totalItems)
    logger.println("Downloaded $str file(s), ${formatBytes(totalBytes)} in ${formatElapsed(elapsed)}.")
}

private fun httpDownload(connection: HttpURLConnection, target: Target, logger: Logger) : Result {

    logger.print("${connection.url} -> $target ...")

    return when (val code = connection.responseCode) {
        HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_PARTIAL -> {
            val start = System.currentTimeMillis()
            val bytes = transfer(connection.inputStream, target, logger)
            val elapsed = System.currentTimeMillis() - start
            logger.println(" ${formatBytes(bytes)} in ${formatElapsed(elapsed)}.")
            Result.Success(bytes, elapsed)
        }
        else -> {
            logger.println(" error $code.")
            Result.Error(code)
        }
    }
}

private fun transfer(input: InputStream, target: Target, logger: Logger): Long {

    val source = input.source().buffer()
    val sink = target.open().sink().buffer()

    val counterIncrement = 1024 * 1024 // 1 megabyte

    return source.use { inSource ->
        sink.use { outSink ->

            var total: Long = 0
            var counter = 1
            val byteArray = ByteArray(8192)
            var b = inSource.read(byteArray, 0, byteArray.size)

            while (b > 0) {
                outSink.write(byteArray, 0, b)
                total += b.toLong()
                if (counter * counterIncrement < total) {
                    // print a period for each megabyte downloaded as an indeterminate progress indicator
                    logger.print(".")
                    counter++
                }
                b = inSource.read(byteArray, 0, byteArray.size)
            }

            total
        }
    }
}

private fun formatBytes(bytes: Long) : String {

    if (bytes < 1024) {
        return String.format("%,d bytes", bytes)
    }

    val kbytes = bytes / 1024f

    if (kbytes < 1024f) {
        return String.format("%,.1f KB", kbytes)
    }

    val mbytes = kbytes / 1024f

    return String.format("%,.1f MB", mbytes)
}

private fun formatElapsed(elapsed: Long) : String {

    val seconds = elapsed / 1000
    val minutes = seconds / 60

    if (minutes > 0) {
        return "${minutes}m ${seconds % 60}s"
    }

    if (seconds > 0) {
        return "${seconds}s"
    }

    return "${elapsed}ms"
}
