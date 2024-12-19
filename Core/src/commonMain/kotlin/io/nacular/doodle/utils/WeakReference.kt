package io.nacular.doodle.utils

internal expect class WeakReference<T: Any>(value: T) {
    operator fun invoke(): T?
}