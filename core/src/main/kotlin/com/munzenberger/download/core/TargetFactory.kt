package com.munzenberger.download.core

import java.io.File
import java.io.IOException
import java.net.URL

interface TargetFactory {
    fun targetFor(source: URL): Target
}

class FileTargetFactory(private val file: File, private val append: Boolean = false) : TargetFactory {
    constructor(path: String, append: Boolean = false) : this(File(path), append)
    override fun targetFor(source: URL)  = FileTarget(file, append)
}

/**
 * Generates a target that writes to file named by the URL path in the specified base directory.
 */
class FlatPathFileTargetFactory(private val baseDir: File) : TargetFactory {
    constructor(baseDir: String) : this(File(baseDir))
    override fun targetFor(source: URL): Target {

        if (!baseDir.exists() && !baseDir.mkdirs()) {
            throw IOException("Could not mkdirs for baseDir $baseDir")
        }

        val filename = StringBuilder(source.host)
        val parts = source.path.split('/')

        parts.forEach {
            filename.append('_').append(it)
        }

        val path = "${baseDir.path}${File.separator}$filename"

        return FileTarget(path)
    }
}

/**
 * Generates a target that writes a file to a directory structure based on the source URL.
 */
class URLPathFileTargetFactory(private val baseDir: File) : TargetFactory {
    constructor(baseDir: String) : this(File(baseDir))
    override fun targetFor(source: URL): Target {

        val path = StringBuilder(baseDir.path)
                .append(File.separator)
                .append(source.host)

        val parts = source.path.split('/')

        // the last part is the filename
        parts.take(parts.size-1).forEach {
            path.append(File.separator).append(it)
        }

        File(path.toString()).apply {
            if (!exists() && !mkdirs()) {
                throw IOException("Could not mkdirs for $path")
            }
        }

        parts.last().apply {
            path.append(File.separator).append(this)
        }

        return FileTarget(path.toString())
    }
}
