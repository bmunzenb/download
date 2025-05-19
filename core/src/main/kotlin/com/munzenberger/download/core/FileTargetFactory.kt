package com.munzenberger.download.core

import java.net.URL
import java.nio.file.Path

class FileTargetFactory(
    private val path: Path,
    private val append: Boolean = false,
    private val bufferSize: Int = FileTarget.DEFAULT_BUFFER_SIZE,
) : TargetFactory {
    override fun create(source: URL) = FileTarget(path, append, bufferSize)
}

class URLPathFileTargetFactory(
    private val baseDir: Path,
    private val pathSpec: PathSpec = FilenamePathSpec,
    private val bufferSize: Int = FileTarget.DEFAULT_BUFFER_SIZE,
) : TargetFactory {
    override fun create(source: URL): Target {
        val path = pathSpec.pathFor(baseDir, source)
        return FileTarget(path, append = false, bufferSize = bufferSize)
    }
}
