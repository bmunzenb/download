package com.munzenberger.download.app

import com.munzenberger.download.core.FileTargetFactory
import com.munzenberger.download.core.RangeParamIterator
import com.munzenberger.download.core.TemplateURLQueue
import com.munzenberger.download.core.URLQueue
import com.munzenberger.download.core.download

fun main(args: Array<String>) {

    val params = RangeParamIterator(1, 100)

    val queue = TemplateURLQueue("http://example.com/%d", params)

    val targetFactory = FileTargetFactory("/Users/bmunzenb/Desktop/poop.jpg")

    download(queue, targetFactory)
}
