package io.nacular.doodle.theme

import io.nacular.doodle.core.View

/**
 * Created by Nicholas Eddy on 2/13/18.
 */
public class CompositeTheme(vararg themes: Theme): Theme {
    private var themes = mutableSetOf(*themes)

    override fun install(scene: Scene): Unit = themes.forEach { it.install(scene) }

    override fun install(view: View) {
        themes.forEach { it.install(view) }
    }

    public operator fun plus(other: Theme): CompositeTheme = this.apply { themes.plusAssign(other) }

    override fun toString(): String = themes.joinToString(" + ") { "$it" }
}

public operator fun Theme.plus(other: Theme): CompositeTheme = CompositeTheme(this, other)