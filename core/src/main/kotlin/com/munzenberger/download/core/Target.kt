package com.munzenberger.download.core

import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

fun interface Target {
    fun open(): OutputStream
}

class FileTarget(
    val path: Path,
    private val append: Boolean = false,
) : Target {
    @Suppress("SpreadOperator")
    override fun open(): OutputStream {
        val openOptions =
            buildList {
                add(StandardOpenOption.CREATE)
                if (append) {
                    add(StandardOpenOption.APPEND)
                }
            }.toTypedArray()

        return Files.newOutputStream(path, *openOptions)
    }

    override fun toString() = path.toString()
}
