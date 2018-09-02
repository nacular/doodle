package com.nectar.doodle.theme

import com.nectar.doodle.core.Display
import com.nectar.doodle.core.Gizmo

/**
 * Created by Nicholas Eddy on 2/13/18.
 */
class CompositeTheme(vararg themes: Theme): Theme {
    private var themes = setOf(*themes)

    override fun install(display: Display, all: Sequence<Gizmo>) = all.forEach { gizmo -> themes.forEach { it.install  (display, sequenceOf(gizmo)) } }

    operator fun plus(other: Theme) = this.apply { themes += other }

    override fun toString() = themes.map { it.toString() }.joinToString(", ")
}

operator fun Theme.plus(other: Theme) = CompositeTheme(this, other)