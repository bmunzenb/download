package com.munzenberger.download.core

import java.net.URL
import java.nio.file.Path

fun interface PathSpec {
    fun pathFor(
        baseDir: Path,
        source: URL,
    ): Path
}

object FullPathSpec : PathSpec {
    override fun pathFor(
        baseDir: Path,
        source: URL,
    ): Path {
        val parts =
            source.path
                .split("/")
                .filter { it.isNotEmpty() }

        val root = baseDir.resolve(source.host)
        return parts.fold(root) { acc, part -> acc.resolve(part) }
    }
}

object FilenamePathSpec : PathSpec {
    override fun pathFor(
        baseDir: Path,
        source: URL,
    ): Path {
        val filename =
            source.path
                .split("/")
                .last { it.isNotEmpty() }

        return baseDir.resolve(filename)
    }
}
