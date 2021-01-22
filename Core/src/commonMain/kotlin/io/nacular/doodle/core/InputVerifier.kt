package io.nacular.doodle.core


public interface InputVerifier<in T: View> {
    public fun shouldYieldFocus(view: T): Boolean
}