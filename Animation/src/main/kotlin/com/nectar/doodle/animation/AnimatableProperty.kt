package com.nectar.doodle.animation

import com.nectar.doodle.units.Length
import com.nectar.doodle.units.Measure

/**
 * Created by Nicholas Eddy on 3/29/18.
 */
class AnimatableProperty(val name: String, val initialValue: Measure<Length>) {
    init {
        require(name.isNotBlank()) { "name cannot be blank" }
    }
}