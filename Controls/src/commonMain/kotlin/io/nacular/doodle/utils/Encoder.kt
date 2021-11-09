package io.nacular.doodle.utils

import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

public interface Encoder<A, B> {
    @Deprecated(message = "Use `to` instead", replaceWith = ReplaceWith("to"))
    public fun encode(a: A): B?

    @Deprecated(message = "Use `from` instead", replaceWith = ReplaceWith("from"))
    public fun decode(b: B): A?

    public fun to  (a: A): Result<B> = encode(a)?.let { success(it) } ?: failure(IllegalArgumentException("Cannot encode $a"))
    public fun from(b: B): Result<A> = decode(b)?.let { success(it) } ?: failure(IllegalArgumentException("Cannot decode $b"))
}

public class PassThroughEncoder<T>: Encoder<T, T> {
    override fun encode(a: T): T = a
    override fun decode(b: T): T = b

    public override fun to  (a: T): Result<T> = success(a)
    public override fun from(b: T): Result<T> = success(b)
}

public abstract class ToStringEncoder<T>: Encoder<T, String> {
    override fun encode(a: T): String = a.toString()

    public override fun to(a: T): Result<String> = success(a.toString())
}

public object ToStringIntEncoder: ToStringEncoder<Int>() {
    override fun decode(b: String): Int? = b.toIntOrNull()

    public override fun from(b: String): Result<Int> = b.toIntOrNull()?.let { success(it) } ?: failure(IllegalArgumentException("Cannot parse: $b"))
}