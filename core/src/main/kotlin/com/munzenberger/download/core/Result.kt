package com.munzenberger.download.core

sealed class Result {
    data object First : Result()

    data class Success(
        val bytes: Long,
    ) : Result()

    data object SourceNotSupported : Result()

    data class Error(
        val code: Int,
    ) : Result()
}
