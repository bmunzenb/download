package com.munzenberger.download.app

import com.munzenberger.download.core.FlatPathFileTargetFactory
import com.munzenberger.download.core.Processor
import com.munzenberger.download.core.URLQueue
import java.io.File

fun main() {

    val queue = URLQueue.of(
        "http://example.com/index.html",
        "http://example.com/404.html"
    )

    val targetFactory = FlatPathFileTargetFactory(baseDir = File("."))

    Processor().download(queue, targetFactory)
}
