package com.nectar.doodle.core


interface InputVerifier<in T: View> {
    fun shouldYieldFocus(view: T): Boolean
}