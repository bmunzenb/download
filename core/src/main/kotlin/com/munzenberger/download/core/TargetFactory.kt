package com.munzenberger.download.core

import java.io.File
import java.net.URL

interface TargetFactory {
    fun targetFor(source: URL): Target
}

class FileTargetFactory(private val file: File, private val append: Boolean = false) : TargetFactory {
    constructor(path: String, append: Boolean = false) : this(File(path), append)
    override fun targetFor(source: URL)  = FileTarget(file, append)
}
