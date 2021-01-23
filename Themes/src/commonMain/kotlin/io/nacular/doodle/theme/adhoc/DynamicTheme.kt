package io.nacular.doodle.theme.adhoc

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.theme.Modules.BehaviorResolver
import io.nacular.doodle.theme.Modules.BehaviorResult.Matched
import io.nacular.doodle.theme.Theme

public open class DynamicTheme protected constructor(behaviors: Iterable<BehaviorResolver>): Theme {
    private val behaviors = behaviors.reversed()

    override fun install(display: Display, all: Sequence<View>): Unit = all.forEach { view ->
        behaviors.firstOrNull { it(view) == Matched }
    }

    override fun toString(): String = this::class.simpleName ?: ""
}