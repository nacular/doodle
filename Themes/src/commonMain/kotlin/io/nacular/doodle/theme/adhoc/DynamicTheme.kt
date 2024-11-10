package io.nacular.doodle.theme.adhoc

import io.nacular.doodle.core.View
import io.nacular.doodle.theme.Modules.BehaviorResolver
import io.nacular.doodle.theme.Modules.BehaviorResult.Matched
import io.nacular.doodle.theme.Scene
import io.nacular.doodle.theme.Theme

public open class DynamicTheme protected constructor(internal val behaviors: List<BehaviorResolver>): Theme {
    override fun install(scene: Scene): Unit = scene.forEachView(::install)

    override fun install(view: View) {
        behaviors.lastOrNull { it(view) == Matched }
    }

    override fun toString(): String = this::class.simpleName ?: ""
}

private class CompositeDynamicTheme(private val first: DynamicTheme, private val second: DynamicTheme): DynamicTheme(first.behaviors + second.behaviors) {
    override fun selected() {
        first.selected ()
        second.selected()

        super.selected()
    }

    override fun deselected() {
        second.deselected()
        first.deselected ()

        super.deselected()
    }

    override fun toString() = "$first + $second"
}

// FIXME: Remove?
public operator fun DynamicTheme.plus(other: DynamicTheme): DynamicTheme = CompositeDynamicTheme(this, other)