package com.munzenberger.download.core

import java.net.URL

open class ConsoleLogger {

    companion object {
        private const val PROGRESS_COUNTER_INCREMENT = 1024 * 1024
    }

    val callback : Callback = { onStatus(it) }

    private var fileCounter : Int = 0
    private var progressCounter : Int = 0
    private var queueStart : Long = 0
    private var downloadStart : Long = 0
    private var totalBytes : Long = 0

    open fun onStatus(status: Status) {
        when (status) {
            is Status.QueueStarted -> onQueueStarted()
            is Status.DownloadStarted -> onDownloadStarted(status.url, status.target)
            is Status.DownloadProgress -> onDownloadProgress(status.bytes)
            is Status.DownloadResult -> onDownloadResult(status.url, status.target, status.result)
            is Status.QueueCompleted -> onQueueCompleted()
        }
    }

    open fun onQueueStarted() {
        queueStart = System.currentTimeMillis()
        fileCounter = 0
        progressCounter = 0
        totalBytes = 0
        println("Starting download ...")
    }

    open fun onDownloadStarted(url: URL, target: Target) {
        downloadStart = System.currentTimeMillis()
        progressCounter = 1
        print("[${fileCounter+1}] $url -> $target ...")
    }

    open fun onDownloadProgress(bytes: Long) {
        if (progressCounter * PROGRESS_COUNTER_INCREMENT < bytes) {
            print(".") // print a period for each megabyte downloaded as an indeterminate progress indicator
            progressCounter++
        }
    }

    open fun onDownloadResult(url: URL, target: Target, result: Result) {
        when (result) {

            is Result.Error ->
                println(" error ${result.code}.")

            is Result.SourceNotSupported ->
                println(" source not supported: $url")

            is Result.Success -> {
                fileCounter++
                totalBytes += result.bytes
                val elapsed = System.currentTimeMillis() - downloadStart
                println(" ${result.bytes.formatBytes} in ${elapsed.formatElapsed}.")
            }

            else ->
                println(" $result.")
        }
    }

    open fun onQueueCompleted() {
        val str = String.format("%,d", fileCounter)
        val elapsed = System.currentTimeMillis() - queueStart
        println("Downloaded $str file(s), ${totalBytes.formatBytes} in ${elapsed.formatElapsed}.")
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

        return String.format("%,.1f MB", mb)
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
