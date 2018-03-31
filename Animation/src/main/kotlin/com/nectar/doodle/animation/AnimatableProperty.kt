package com.nectar.doodle.animation

/**
 * Created by Nicholas Eddy on 3/29/18.
 */
class AnimatableProperty(val name: String, val initialValue: Double) {
    init {
        require(name.isNotBlank()) { "name cannot be blank" }
    }
}