package com.munzenberger.download.core

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

interface Target {
    fun open(): OutputStream
}

class FileTarget(
    val file: File,
    private val append: Boolean = false,
) : Target {
    constructor(path: String, append: Boolean = false) : this(File(path), append)

    override fun open(): OutputStream = FileOutputStream(file, append)

    override fun toString() = file.toString()
}
