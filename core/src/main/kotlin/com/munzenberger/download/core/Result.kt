package com.munzenberger.download.core

sealed class Result {

    data object Init : Result()

    data class Success(val target: Target) : Result();

    data object SourceNotSupported : Result();

    class Error(val code: Int) : Result();
}
