package com.munzenberger.download.core

import java.io.InputStream
import java.util.function.Consumer

interface Target {
    val name: String

    fun write(
        inStream: InputStream,
        progressCallback: Consumer<Long>,
    ): Long
}
