package com.nectar.doodle.utils

interface Encoder<A, B> {
    fun encode(a: A): B?
    fun decode(b: B): A?
}

class PassThroughEncoder<T>: Encoder<T, T> {
    override fun encode(a: T) = a
    override fun decode(b: T) = b
}

abstract class ToStringEncoder<T>: Encoder<T, String> {
    override fun encode(a: T) = a.toString()
}

object ToStringIntEncoder: ToStringEncoder<Int>() {
    override fun decode(b: String) = b.toIntOrNull()
}