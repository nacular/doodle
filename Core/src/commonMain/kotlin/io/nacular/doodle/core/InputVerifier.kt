package io.nacular.doodle.core


@Deprecated("Not used")
public interface InputVerifier<in T: View> {
    public fun shouldYieldFocus(view: T): Boolean
}