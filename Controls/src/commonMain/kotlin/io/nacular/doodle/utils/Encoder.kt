package io.nacular.doodle.utils

public interface Encoder<A, B> {
    public fun encode(a: A): B?
    public fun decode(b: B): A?
}

public class PassThroughEncoder<T>: Encoder<T, T> {
    override fun encode(a: T): T = a
    override fun decode(b: T): T = b
}

public abstract class ToStringEncoder<T>: Encoder<T, String> {
    override fun encode(a: T): String = a.toString()
}

public object ToStringIntEncoder: ToStringEncoder<Int>() {
    override fun decode(b: String): Int? = b.toIntOrNull()
}