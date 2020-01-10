package com.munzenberger.download.core

import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection

fun download(source: Source<Download?>, logger: Logger = ConsoleLogger()) {

    var result = Result.SUCCESS

    var download = source.next(result)

    while (download != null) {

        val connection = download.source.openConnection()

        result = when (connection) {
            is HttpURLConnection -> httpDownload(connection, download.target, logger)
            else -> {
                logger.println("Unsupported connection type: ${connection::class.java.simpleName}")
                Result.SOURCE_NOT_SUPPORTED
            }
        }

        download = source.next(result)
    }
}

private fun httpDownload(connection: HttpURLConnection, target: Target, logger: Logger) : Result {

    logger.print("${connection.url} -> $target ... ")

    return when (val code = connection.responseCode) {
        HttpURLConnection.HTTP_OK -> {
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

private fun transfer(input: InputStream, target: Target): Int {

    val out = target.open()

    var totalBytes = 0

    val buffer = ByteArray(4096)

    var bytes = input.read(buffer)
    while (bytes > 0) {
        out.write(buffer, 0, bytes)
        totalBytes += bytes
        bytes = input.read(buffer)
    }

    target.close()

    return totalBytes
}

private fun formatBytes(bytes: Int) : String {

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
