package com.munzenberger.download.app

import com.munzenberger.download.core.FileTargetFactory
import com.munzenberger.download.core.URLQueue
import com.munzenberger.download.core.download
import java.io.File

fun main() {

    val queue = URLQueue.of("http://example.com")

    val targetFactory = FileTargetFactory(File("/Users/example/Desktop/target.txt"))

    download(queue, targetFactory)
}
