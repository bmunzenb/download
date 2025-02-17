package com.munzenberger.download.core

import java.net.URL
import java.util.Locale

private const val ONE_MEGABYTE = 1024 * 1024

class LoggingStatusConsumer(
    private val locale: Locale = Locale.getDefault(),
    private val progressIndicatorIncrementInBytes: Int = ONE_MEGABYTE,
) : StatusConsumer {
    private var urlCounter: Int = 0
    private var downloadCounter: Int = 0
    private var progressCounter: Int = 0
    private var queueStart: Long = 0
    private var downloadStart: Long = 0
    private var totalBytes: Long = 0

    override fun onQueueStarted(queue: URLQueue) {
        queueStart = System.currentTimeMillis()
        urlCounter = 0
        downloadCounter = 0
        progressCounter = 0
        totalBytes = 0
        println("Starting download ...")
    }

    override fun onDownloadStarted(
        url: URL,
        target: Target,
    ) {
        downloadStart = System.currentTimeMillis()
        urlCounter++
        progressCounter = 1
        print("[$urlCounter] $url -> ${target.name} ...")
    }

    override fun onDownloadProgress(
        url: URL,
        target: Target,
        bytes: Long,
    ) {
        if (progressCounter * progressIndicatorIncrementInBytes < bytes) {
            print(".") // print a period for each megabyte downloaded as an indeterminate progress indicator
            progressCounter++
        }
    }

    override fun onDownloadResult(
        url: URL,
        target: Target,
        result: Result<ResultData>,
    ) {
        result.onSuccess { data ->
            totalBytes += data.bytes
            val elapsed = System.currentTimeMillis() - downloadStart
            println(" ${data.bytes.formatBytes(locale)} in ${elapsed.formatElapsed}.")
            downloadCounter++
        }

        result.onFailure { error ->
            when (error) {
                is HttpException -> {
                    println(" HTTP ${error.code}.")
                }
                else -> {
                    println(" ${error.message}.")
                }
            }
        }
    }

    override fun onQueueCompleted(queue: URLQueue) {
        val urls = String.format(locale, "%,d", urlCounter)
        val downloads = String.format(locale, "%,d", downloadCounter)
        val elapsed = System.currentTimeMillis() - queueStart
        println("Download completed in ${elapsed.formatElapsed}.")
        println("$urls URL(s): $downloads downloaded, ${totalBytes.formatBytes(locale)}.")
    }
}

private const val BYTE_BOUNDARY = 1024f

@Suppress("ReturnCount")
fun Long.formatBytes(locale: Locale): String {
    if (this < BYTE_BOUNDARY) {
        return String.format(locale, "%,d bytes", this)
    }

    val kb = this / BYTE_BOUNDARY

    if (kb < BYTE_BOUNDARY) {
        return String.format(locale, "%,.1f KB", kb)
    }

    val mb = kb / BYTE_BOUNDARY

    if (mb < BYTE_BOUNDARY) {
        return String.format(locale, "%,.1f MB", mb)
    }

    val gb = mb / BYTE_BOUNDARY

    return String.format(locale, "%,.1f GB", gb)
}

private const val MILLIS_PER_SECOND = 1000
private const val SECONDS_PER_MINUTE = 60

val Long.formatElapsed: String
    get() {
        val seconds = this / MILLIS_PER_SECOND
        val minutes = seconds / SECONDS_PER_MINUTE

        if (minutes > 0) {
            return "$minutes m ${seconds % SECONDS_PER_MINUTE} s"
        }

        if (seconds > 0) {
            return "$seconds s"
        }

        return "$this ms"
    }
