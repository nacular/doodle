package io.nacular.doodle.utils

/**
 * Simple utility for generating identifiers that are unique within a
 * single app instance.
 */
internal interface IdGenerator {
    fun nextId(): Int
}

internal class SimpleIdGenerator: IdGenerator {
    private var currentId = 0

    override fun nextId(): Int = currentId++
}