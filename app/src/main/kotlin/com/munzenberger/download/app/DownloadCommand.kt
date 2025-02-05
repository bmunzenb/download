package com.munzenberger.download.app

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import com.munzenberger.download.core.FileTargetFactory
import com.munzenberger.download.core.IncrementUntilErrorParamIterator
import com.munzenberger.download.core.IntRangeParamIterator
import com.munzenberger.download.core.Processor
import com.munzenberger.download.core.TemplateURLQueue
import com.munzenberger.download.core.URLPathFileTargetFactory
import java.nio.file.Path

class DownloadCommand : CliktCommand() {
    private val template by option("--template")
        .required()
        .help("Incrementing URL template")

    private val incrementStart by option("--start")
        .int()
        .default(0)
        .help("Start value (defaults to 0)")

    private val incrementEnd by option("--end")
        .int()
        .help("End value, otherwise increment until error")

    private val appendOutputPath by option("--append")
        .path()
        .help("Appends each download to this file")

    private val userAgent by option("--user-agent")
        .help("Value for user-agent request header")

    private val referer by option("--referer")
        .help("Value for referer request header")

    override fun run() {
        val paramIterator =
            when (val end = incrementEnd) {
                null -> IncrementUntilErrorParamIterator(incrementStart)
                else -> IntRangeParamIterator(incrementStart..end)
            }

        val queue = TemplateURLQueue(template, paramIterator)

        val targetFactory =
            when (val path = appendOutputPath) {
                null -> URLPathFileTargetFactory(Path.of("."))
                else -> FileTargetFactory(path, true)
            }

        val requestProperties =
            buildMap {
                userAgent?.let { put("User-agent", it) }
                referer?.let { put("Referer", it) }
            }

        Processor(requestProperties).download(queue, targetFactory)
    }
}
