package io.nacular.doodle.utils

public actual fun <K, V> fastMutableMapOf(): MutableMap<K, V> = mutableMapOf() // Just use normal impl on JVM
public actual fun <E>    fastMutableSetOf(): MutableSet<E>    = mutableSetOf() // Just use normal impl on JVM