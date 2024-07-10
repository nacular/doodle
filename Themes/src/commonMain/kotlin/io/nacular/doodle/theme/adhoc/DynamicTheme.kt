package io.nacular.doodle.theme.adhoc

import io.nacular.doodle.core.View
import io.nacular.doodle.theme.Modules.BehaviorResolver
import io.nacular.doodle.theme.Modules.BehaviorResult.Matched
import io.nacular.doodle.theme.Scene
import io.nacular.doodle.theme.Theme

public open class DynamicTheme protected constructor(internal val behaviors: Iterable<BehaviorResolver>): Theme {
    override fun install(scene: Scene): Unit = scene.forEachView(::install)

    override fun install(view: View) {
        behaviors.lastOrNull {
            it(view) == Matched
        }
    }

    override fun toString(): String = this::class.simpleName ?: ""
}

// FIXME: Remove?
public operator fun DynamicTheme.plus(other: DynamicTheme): DynamicTheme = object: DynamicTheme(behaviors + other.behaviors) {
    override fun toString() = "${this@plus} + $other"
}