package io.nacular.doodle.controls.range

/**
 * A function f(x) that also has a valid f-1(x). The domain and range of this function must be
 * [0-1].
 */
public interface InvertibleFunction {
    /**
     * @return the output of [value] that is in [0-1]
     */
    public operator fun invoke (value: Float): Float

    /**
     * Inverse of [invoke].
     *
     * @return the output of [value] that is in [0-1]
     */
    public fun inverse(value: Float): Float
}

/**
 * [InvertibleFunction] f(x) = x
 */
public object LinearFunction: InvertibleFunction {
    override fun invoke (value: Float): Float = value
    override fun inverse(value: Float): Float = value
}