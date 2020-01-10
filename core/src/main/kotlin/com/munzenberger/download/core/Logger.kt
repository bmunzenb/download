package com.munzenberger.download.core

interface Logger {
    fun print(message: String)
    fun println(message: String)
}

class ConsoleLogger : Logger {
    override fun print(message: String) = kotlin.io.print(message)
    override fun println(message: String) = kotlin.io.println(message)
}
