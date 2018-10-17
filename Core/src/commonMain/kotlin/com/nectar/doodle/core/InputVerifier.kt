package com.nectar.doodle.core


interface InputVerifier<in T: Gizmo> {
    fun shouldYieldFocus(gizmo: T): Boolean
}
