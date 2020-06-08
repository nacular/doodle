package io.nacular.doodle.theme.adhoc

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.theme.Modules.BehaviorResolver
import io.nacular.doodle.theme.Modules.BehaviorResult.Matched
import io.nacular.doodle.theme.Theme

open class DynamicTheme protected constructor(behaviors: Iterable<BehaviorResolver>): Theme {
    private val behaviors = behaviors.reversed()

    override fun install(display: Display, all: Sequence<View>) = all.forEach { view ->
        behaviors.firstOrNull { it(view) == Matched }
    }

    override fun toString() = this::class.simpleName ?: ""
}