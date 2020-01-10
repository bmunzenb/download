package com.munzenberger.download.core

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URL

data class Download(val source: URL, val target: Target)

interface Target {
    fun open(): OutputStream
    fun close()
}

class FileTarget(private val file: File, private val append: Boolean = false) : Target {

    constructor(path: String, append: Boolean = false) : this(File(path), append)

    private var out: OutputStream? = null

    override fun open() : OutputStream {
        return FileOutputStream(file, append).also {
            out = it
        }
    }
    override fun close() {
        out?.close().also {
            out = null
        }
    }

    override fun toString() = file.toString()
}
