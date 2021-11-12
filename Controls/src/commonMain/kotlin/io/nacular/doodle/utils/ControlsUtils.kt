package io.nacular.doodle.utils

/**
 * Simple function that generates a value R given the context of T.
 * This is useful in cases where a property of T is needed:
 *
 * ```kotlin
 *
 * interface Person {
 *     val name: String
 *     val age: Int
 * }
 *
 * val nameExtractor: Extractor<Person, String> = { name }
 *
 * ```
 */
public typealias Extractor<T, R> = T.() -> R