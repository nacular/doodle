package com.nectar.doodle.theme.adhoc

import com.nectar.doodle.core.Display
import com.nectar.doodle.core.View
import com.nectar.doodle.theme.Theme
import com.nectar.doodle.theme.Modules.BehaviorResolver
import com.nectar.doodle.theme.Modules.BehaviorResult.Matched

open class AdhocTheme protected constructor(behaviors: Iterable<BehaviorResolver>): Theme {
    private val behaviors = behaviors.reversed()

    override fun install(display: Display, all: Sequence<View>) = all.forEach { view ->
        behaviors.firstOrNull { it(view) == Matched }
    }

    override fun toString() = this::class.simpleName ?: ""
}