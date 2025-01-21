package com.munzenberger.download.core

import java.io.File
import java.net.URL

interface TargetFactory {
    fun create(source: URL): Target
}

class FileTargetFactory(
    private val file: File,
    private val append: Boolean = false,
) : TargetFactory {
    override fun create(source: URL) = FileTarget(file, append)
}

class FlatPathFileTargetFactory(
    private val baseDir: File,
    private val delimiter: Char = '_',
) : TargetFactory {
    override fun create(source: URL): Target {
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            error("Could not mkdirs for $baseDir")
        }

        val filename = StringBuilder(source.host)

        val parts =
            source.path
                .split('/')
                .filter { it.isNotEmpty() }

        parts.forEach {
            filename.append(delimiter).append(it)
        }

        val path = baseDir.path + File.separator + filename

        return FileTarget(path)
    }
}

class URLPathFileTargetFactory(
    private val baseDir: File,
) : TargetFactory {
    override fun create(source: URL): Target {
        val path =
            StringBuilder(baseDir.path)
                .append(File.separator)
                .append(source.host)

        val parts =
            source.path
                .split('/')
                .filter { it.isNotEmpty() }

        // the last part is the filename
        parts.take(parts.size - 1).forEach {
            path.append(File.separator).append(it)
        }

        File(path.toString()).apply {
            if (!exists() && !mkdirs()) {
                error("Could not mkdirs for $this")
            }
        }

        parts.last().apply {
            path.append(File.separator).append(this)
        }

        return FileTarget(path.toString())
    }
}
