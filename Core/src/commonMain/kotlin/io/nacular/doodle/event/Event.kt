package io.nacular.doodle.event

public abstract class Event<out T> protected constructor(public val source: T) {
    /**
     * Indicates whether the event has been consumed.
     *
     * @return true if consumed
     */
    public var consumed: Boolean = false
        private set

    /**
     * Sets an event's state to consumed. Consuming an event prevents other listeners
     * and the OS from handling it.
     */
    public fun consume() {
        consumed = true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Event<*>) return false

        if (source   != other.source  ) return false
        if (consumed != other.consumed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source?.hashCode() ?: 0
        result = 31 * result + consumed.hashCode()
        return result
    }

    override fun toString(): String = "${this::class.simpleName} from $source"
}
