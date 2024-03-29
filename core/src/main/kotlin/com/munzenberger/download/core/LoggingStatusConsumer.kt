package com.munzenberger.download.core

import java.net.URL

class LoggingStatusConsumer : StatusConsumer {

    companion object {
        private const val PROGRESS_COUNTER_INCREMENT = 1024 * 1024
    }

    private var urlCounter : Int = 0
    private var downloadCounter : Int = 0
    private var progressCounter : Int = 0
    private var queueStart : Long = 0
    private var downloadStart : Long = 0
    private var totalBytes : Long = 0

    override fun onQueueStarted(queue: URLQueue) {
        queueStart = System.currentTimeMillis()
        urlCounter = 0
        downloadCounter = 0
        progressCounter = 0
        totalBytes = 0
        println("Starting download ...")
    }

    override fun onDownloadStarted(url: URL, target: Target) {
        downloadStart = System.currentTimeMillis()
        urlCounter++
        progressCounter = 1
        print("[$urlCounter] $url -> $target ...")
    }

    override fun onDownloadProgress(url: URL, target: Target, bytes: Long) {
        if (progressCounter * PROGRESS_COUNTER_INCREMENT < bytes) {
            print(".") // print a period for each megabyte downloaded as an indeterminate progress indicator
            progressCounter++
        }
    }

    override fun onDownloadResult(url: URL, target: Target, result: Result) {
        when (result) {

            is Result.Error ->
                println(" error ${result.code}.")

            is Result.SourceNotSupported ->
                println(" source not supported: $url")

            is Result.Success -> {
                totalBytes += result.bytes
                val elapsed = System.currentTimeMillis() - downloadStart
                println(" ${result.bytes.formatBytes} in ${elapsed.formatElapsed}.")
                downloadCounter++
            }

            else ->
                println(" $result.")
        }
    }

    override fun onQueueCompleted(queue: URLQueue) {
        val urls = String.format("%,d", urlCounter)
        val downloads = String.format("%,d", downloadCounter)
        val elapsed = System.currentTimeMillis() - queueStart
        println("Download completed in ${elapsed.formatElapsed}.")
        println("$urls URL(s): $downloads downloaded, ${totalBytes.formatBytes}.")
    }
}

private val Long.formatBytes : String
    get() {
        if (this < 1024) {
            return String.format("%,d bytes", this)
        }

        val kb = this / 1024f

        if (kb < 1024f) {
            return String.format("%,.1f KB", kb)
        }

        val mb = kb / 1024f

        if (mb < 1024f) {
            return String.format("%,.1f MB", mb)
        }

        val gb = mb / 1024f

        return String.format("%,.1f GB", gb)
    }

private val Long.formatElapsed : String
    get()  {
        val seconds = this / 1000
        val minutes = seconds / 60

        if (minutes > 0) {
            return "$minutes m ${seconds % 60} s"
        }

        if (seconds > 0) {
            return "$seconds s"
        }

        return "$this ms"
    }
