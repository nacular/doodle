package io.nacular.doodle.utils

/**
 * Simple utility for generating identifiers that are unique within a
 * single app instance.
 */
internal interface IdGenerator {
    fun nextId(): String
}

internal class SimpleIdGenerator(private val seed: String): IdGenerator {
    private var currentId = 0

    override fun nextId(): String = "i$seed${currentId++}"
}