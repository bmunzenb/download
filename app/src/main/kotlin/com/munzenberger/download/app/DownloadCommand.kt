package com.munzenberger.download.app

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.munzenberger.download.core.*
import java.io.File

class DownloadCommand : CliktCommand() {

    private val template by option("--increment-template")
        .required()
        .help("Incrementing URL template")

    private val incrementStart by option("--increment-start")
        .int()
        .default(0)
        .help("Start value (defaults to 0)")

    private val incrementEnd by option("--increment-end")
        .int()
        .help("End value, otherwise increment until error")

    private val appendOutputFile by option("--append-to-file")
        .file()
        .help("Appends each download to this file")

    private val userAgent by option("--user-agent")
        .help("Value for user-agent request header")

    private val referer by option("--referer")
        .help("Value for referer request header")

    override fun run() {

        val paramIterator = when (val end = incrementEnd) {
            null -> IncrementUntilErrorParamIterator(incrementStart)
            else -> IntRangeParamIterator(incrementStart..end)
        }

        val queue = TemplateURLQueue(template, paramIterator)

        val targetFactory = when (val file = appendOutputFile) {
            null -> URLPathFileTargetFactory(File("."))
            else -> FileTargetFactory(file, true)
        }

        val requestProperties = mutableMapOf<String,String>().apply {
            userAgent?.let { put("User-agent", it) }
            referer?.let { put("Referer", it) }
        }

        Processor(requestProperties).download(queue, targetFactory)
    }
}
