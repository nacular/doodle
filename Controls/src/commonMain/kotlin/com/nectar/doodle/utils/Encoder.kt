package com.nectar.doodle.utils

interface Encoder<A, B> {
    fun encode(a: A): B?
    fun decode(b: B): A?
}