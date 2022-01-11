package io.nacular.doodle.utils

import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

public interface Encoder<A, B> {
    public fun encode(a: A): Result<B>
    public fun decode(b: B): Result<A>
}

public class PassThroughEncoder<T>: Encoder<T, T> {
    public override fun encode(a: T): Result<T> = success(a)
    public override fun decode(b: T): Result<T> = success(b)
}

public abstract class ToStringEncoder<T>: Encoder<T, String> {
    public override fun encode(a: T): Result<String> = success(a.toString())
}

public object ToStringIntEncoder: ToStringEncoder<Int>() {
    public override fun decode(b: String): Result<Int> = b.toIntOrNull()?.let { success(it) } ?: failure(IllegalArgumentException("Cannot parse: $b"))
}