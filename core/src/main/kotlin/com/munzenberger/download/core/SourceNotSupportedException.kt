package com.munzenberger.download.core

import java.net.URL

class SourceNotSupportedException(
    val url: URL,
) : Exception()
