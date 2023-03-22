package io.dongxi.natty.util

object ClassUtils {

    fun simpleClassName(obj: Any): String {
        return obj::class.simpleName.toString()
    }
}