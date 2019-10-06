package com.nectar.doodle.theme

import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View

/**
 * Created by Nicholas Eddy on 2/13/18.
 */
class CompositeTheme(vararg themes: Theme): Theme {
    private var themes = mutableSetOf(*themes)

    override fun install(display: Display, all: Sequence<View>) = all.forEach { view -> themes.forEach { it.install(display, sequenceOf(view)) } }

    operator fun plus(other: Theme) = this.apply { themes.plusAssign(other) }

    override fun toString() = themes.joinToString(", ") { it.toString() }
}

operator fun Theme.plus(other: Theme) = CompositeTheme(this, other)