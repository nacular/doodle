package com.nectar.doodle.utils

interface Cancelable {
    fun cancel()
}

object NoOpCancelable: Cancelable {
    override fun cancel() {}
}