package io.nacular.doodle.utils

internal actual class WeakReference<T: Any> actual constructor(value: T) {
    private val weakRef = java.lang.ref.WeakReference<T>(value)

    actual operator fun invoke() = weakRef.get()
}