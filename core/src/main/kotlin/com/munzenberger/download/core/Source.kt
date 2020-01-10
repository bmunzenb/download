package com.munzenberger.download.core

interface Source<T> {
    fun next(result: Result) : T
}
