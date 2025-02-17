package com.munzenberger.download.core

import java.net.URL
import java.nio.file.Path

fun interface TargetFactory {
    fun create(source: URL): Target
}

class FileTargetFactory(
    private val path: Path,
    private val append: Boolean = false,
    private val bufferSize: Int = FileTarget.DEFAULT_BUFFER_SIZE,
) : TargetFactory {
    override fun create(source: URL) = FileTarget(path, append, bufferSize)
}

class URLPathFileTargetFactory(
    private val baseDir: Path,
    private val useFullPath: Boolean = false,
    private val bufferSize: Int = FileTarget.DEFAULT_BUFFER_SIZE,
) : TargetFactory {
    override fun create(source: URL): Target {
        val parts =
            source.path
                .split("/")
                .filter { it.isNotEmpty() }

        val path =
            if (useFullPath) {
                val root = baseDir.resolve(source.host)
                parts.fold(root) { acc, part -> acc.resolve(part) }
            } else {
                baseDir.resolve(parts.last())
            }

        return FileTarget(path, append = false, bufferSize = bufferSize)
    }
}
