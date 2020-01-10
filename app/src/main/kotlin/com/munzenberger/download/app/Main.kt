package com.munzenberger.download.app

import com.munzenberger.download.core.Download
import com.munzenberger.download.core.FileTarget
import com.munzenberger.download.core.Result
import com.munzenberger.download.core.Source
import com.munzenberger.download.core.download
import java.net.URL

fun main(args: Array<String>) {

    val source = object : Source<Download?> {

        var hasNext = true

        override fun next(result: Result): Download? {

            var download: Download? = null

            if (hasNext) {
                hasNext = false

                download = Download(
                        URL("https://cdn.shopify.com/s/files/1/0748/6277/products/smellypoopinabox_2000x.jpg?v=1557530195"),
                        FileTarget("/Users/bmunzenb/Desktop/poop.jpg"))
            }

            return download
        }
    }

    download(source)
}
