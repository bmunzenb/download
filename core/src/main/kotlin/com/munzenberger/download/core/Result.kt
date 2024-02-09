package com.munzenberger.download.core

sealed class Result {

    data object First : Result()

    data class Success(val target: Target, val bytes: Long) : Result();

    data object SourceNotSupported : Result();

    class Error(val code: Int) : Result();
}
