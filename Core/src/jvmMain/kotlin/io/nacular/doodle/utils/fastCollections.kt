package io.nacular.doodle.utils

public actual fun <K, V> fastMutableMapOf(): MutableMap<K, V> = mutableMapOf() // Just use normal impl on JVM

public actual fun <E>    fastSetOf       (                  ): Set<E>           = setOf       () // Just use normal impl on JVM
public actual fun <E>    fastMutableSetOf(                  ): MutableSet<E>    = mutableSetOf() // Just use normal impl on JVM
public actual fun <E>    fastSetOf       (vararg elements: E): Set<E>           = setOf       (*elements) // Just use normal impl on JVM
public actual fun <E>    fastMutableSetOf(vararg elements: E): MutableSet<E>    = mutableSetOf(*elements) // Just use normal impl on JVM