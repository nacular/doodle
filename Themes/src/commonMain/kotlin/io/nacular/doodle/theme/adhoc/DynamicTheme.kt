package io.nacular.doodle.theme.adhoc

import io.nacular.doodle.core.View
import io.nacular.doodle.theme.Modules.BehaviorResolver
import io.nacular.doodle.theme.Modules.BehaviorResult.Matched
import io.nacular.doodle.theme.Scene
import io.nacular.doodle.theme.Theme

public open class DynamicTheme protected constructor(behaviors: Iterable<BehaviorResolver>): Theme {
    private val behaviors = behaviors.reversed()

    override fun install(scene: Scene): Unit = scene.forEachView(::install)

    override fun install(view: View) {
        behaviors.firstOrNull { it(view) == Matched }
    }

    override fun toString(): String = this::class.simpleName ?: ""
}