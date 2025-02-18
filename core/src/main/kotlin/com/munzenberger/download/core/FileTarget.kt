package com.munzenberger.download.core

import okio.buffer
import okio.sink
import okio.source
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.function.Consumer

class FileTarget(
    val file: Path,
    private val append: Boolean = false,
    private val bufferSize: Int = DEFAULT_BUFFER_SIZE,
) : Target {
    companion object {
        const val DEFAULT_BUFFER_SIZE = 8192
    }

    override val name = file.toString()

    @Suppress("SpreadOperator")
    override fun write(
        inStream: InputStream,
        progressCallback: Consumer<Long>,
    ): Long {
        file.parent?.run(Files::createDirectories)

        val options: Collection<OpenOption> =
            buildList {
                add(StandardOpenOption.CREATE)
                if (append) {
                    add(StandardOpenOption.APPEND)
                } else {
                    add(StandardOpenOption.TRUNCATE_EXISTING)
                }
            }

        val outStream = Files.newOutputStream(file, *options.toTypedArray())

        val source = inStream.source().buffer()
        val sink = outStream.sink().buffer()

        return source.use { inSource ->
            sink.use { outSink ->

                var total: Long = 0
                val byteArray = ByteArray(bufferSize)
                var b = inSource.read(byteArray, 0, byteArray.size)

                while (b > 0) {
                    outSink.write(byteArray, 0, b)
                    total += b.toLong()
                    progressCallback.accept(total)
                    b = inSource.read(byteArray, 0, byteArray.size)
                }

                total
            }
        }
    }
}
