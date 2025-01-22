package io.nacular.doodle.utils

import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

/**
 * Defines translation logic between two types: [A] and [B].
 */
public interface Encoder<A, B> {
    /**
     * Convert [a] to [B]
     *
     * @param a to be converted
     * @return a Result containing thew new type if successful
     */
    public fun encode(a: A): Result<B>

    /**
     * Convert [b] to [A]
     *
     * @param b to be converted
     * @return a Result containing thew new type if successful
     */
    public fun decode(b: B): Result<A>
}

/**
 * An [Encoder] that "converts" [T] to itself.
 */
public class PassThroughEncoder<T>: Encoder<T, T> {
    public override fun encode(a: T): Result<T> = success(a)
    public override fun decode(b: T): Result<T> = success(b)
}

/**
 * An [Encoder] that converts between [T] and [String][kotlin.String].
 */
public abstract class ToStringEncoder<T>: Encoder<T, String> {
    public override fun encode(a: T): Result<String> = success(a.toString())
}

/**
 * An [Encoder] that converts between [Boolean] and [String].
 */
public object ToStringBooleanEncoder: ToStringEncoder<Boolean>() {
    public override fun decode(b: String): Result<Boolean> = b.toBooleanStrictOrNull()?.let { success(it) } ?: failure(IllegalArgumentException("Cannot parse: $b"))
}

/**
 * An [Encoder] that converts between [Int] and [String].
 */
public object ToStringIntEncoder: ToStringEncoder<Int>() {
    public override fun decode(b: String): Result<Int> = b.toIntOrNull()?.let { success(it) } ?: failure(IllegalArgumentException("Cannot parse: $b"))
}

/**
 * An [Encoder] that converts between [Float] and [String].
 */
public object ToStringFloatEncoder: ToStringEncoder<Float>() {
    public override fun decode(b: String): Result<Float> = b.toFloatOrNull()?.let { success(it) } ?: failure(IllegalArgumentException("Cannot parse: $b"))
}

/**
 * An [Encoder] that converts between [Double] and [String].
 */
public object ToStringDoubleEncoder: ToStringEncoder<Double>() {
    public override fun decode(b: String): Result<Double> = b.toDoubleOrNull()?.let { success(it) } ?: failure(IllegalArgumentException("Cannot parse: $b"))
}

/**
 * An [Encoder] that converts between [Long] and [String].
 */
public object ToStringLongEncoder: ToStringEncoder<Long>() {
    public override fun decode(b: String): Result<Long> = b.toLongOrNull()?.let { success(it) } ?: failure(IllegalArgumentException("Cannot parse: $b"))
}

/**
 * An [Encoder] that converts between [Byte] and [String].
 */
public object ToStringByteEncoder: ToStringEncoder<Byte>() {
    public override fun decode(b: String): Result<Byte> = b.toByteOrNull()?.let { success(it) } ?: failure(IllegalArgumentException("Cannot parse: $b"))
}