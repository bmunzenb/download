package com.munzenberger.download.core

sealed class Result {

    class Success(val bytes: Long, val elapsed: Long) : Result();

    object SourceNotSupported : Result();

    class Error(val code: Int) : Result();
}
