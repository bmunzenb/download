package com.munzenberger.download.core

import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

fun interface TargetFactory {
    fun create(source: URL): Target
}

class FileTargetFactory(
    private val path: Path,
    private val append: Boolean = false,
) : TargetFactory {
    override fun create(source: URL) = FileTarget(path, append)
}

class FlatPathFileTargetFactory(
    private val baseDir: Path,
    private val delimiter: Char = '_',
) : TargetFactory {
    override fun create(source: URL): Target {
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir)
        }

        val filename =
            buildString {
                append(source.host)
                source.pathParts.forEach { append(delimiter).append(it) }
            }

        val path = baseDir.resolve(filename)

        return FileTarget(path)
    }
}

class URLPathFileTargetFactory(
    private val baseDir: Path,
) : TargetFactory {
    override fun create(source: URL): Target {
        val path = baseDir.resolve(source.host)

        val parts = source.pathParts

        // The last part is the filename
        val directoryPath = parts.dropLast(1).fold(path) { acc, part -> acc.resolve(part) }

        // Ensure the directory exists
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath)
        }

        // Append the filename
        val filePath = directoryPath.resolve(parts.last())

        return FileTarget(filePath)
    }
}

private val URL.pathParts: List<String>
    get() = this.path.split("/").filter { it.isNotEmpty() }
