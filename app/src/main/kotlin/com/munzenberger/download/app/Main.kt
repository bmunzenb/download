package com.munzenberger.download.app

import com.munzenberger.download.core.FileTargetFactory
import com.munzenberger.download.core.URLQueue
import com.munzenberger.download.core.download

fun main(args: Array<String>) {

    val queue = URLQueue.of("http://example.com")

    val targetFactory = FileTargetFactory("/Users/example/Desktop")

    download(queue, targetFactory)
}
