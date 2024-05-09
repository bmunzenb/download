package com.munzenberger.download.app

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.munzenberger.download.core.*

class DownloadByIncrementingTemplate : CliktCommand() {

    private val template by option("--template")
        .required()

    private val incrementStart by option("--increment-start")
        .int()
        .default(0)

    private val incrementEnd by option("--increment-end")
        .int()

    private val appendOutputFile by option("--append-to-file")
        .file()
        .required()

    override fun run() {

        val paramIterator = when (val end = incrementEnd) {
            null -> IncrementUntilErrorParamIterator(incrementStart)
            else -> IntRangeParamIterator(incrementStart..end)
        }

        val queue = TemplateURLQueue(template, paramIterator)

        val targetFactory = FileTargetFactory(appendOutputFile, true)

        Processor().download(queue, targetFactory)
    }
}
