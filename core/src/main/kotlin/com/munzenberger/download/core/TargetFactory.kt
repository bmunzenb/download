package com.munzenberger.download.core

import java.net.URL

fun interface TargetFactory {
    fun create(source: URL): Target
}
